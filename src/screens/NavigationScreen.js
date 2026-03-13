import React, { useState, useEffect, useCallback, useRef } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import MapView, { Polyline, Marker } from 'react-native-maps';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation, useRoute } from '@react-navigation/native';
import { useAppContext } from '../store/AppContext';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';
import { formatDuration } from '../constants/trailData';
import EmergencyButton from '../components/EmergencyButton';
import SafetyCheckInModal from '../components/SafetyCheckInModal';
import * as LocationService from '../services/LocationService';
import { checkDeviation } from '../services/GeofencingService';

const NavigationScreen = () => {
  const navigation = useNavigation();
  const route = useRoute();
  const { state, dispatch } = useAppContext();
  const mapRef = useRef(null);
  const trail = route.params?.trail;
  const [userLocation, setUserLocation] = useState(null);
  const [elapsedTime, setElapsedTime] = useState(0);
  const [showCheckIn, setShowCheckIn] = useState(false);
  const [deviation, setDeviation] = useState(null);
  const startTimeRef = useRef(Date.now());

  useEffect(() => {
    if (!state.currentHike.active && trail) {
      dispatch({ type: 'START_HIKE', payload: { trailId: trail.id, trailName: trail.name } });
    }
  }, []);

  // Timer
  useEffect(() => {
    if (!state.currentHike.active || state.currentHike.paused) return;
    const timer = setInterval(() => {
      setElapsedTime(Math.floor((Date.now() - startTimeRef.current) / 1000));
    }, 1000);
    return () => clearInterval(timer);
  }, [state.currentHike.active, state.currentHike.paused]);

  // Location tracking
  useEffect(() => {
    if (!state.currentHike.active) return;
    LocationService.startTracking((loc) => {
      setUserLocation(loc);
      dispatch({ type: 'UPDATE_LOCATION', payload: loc });
      if (trail) {
        const dev = checkDeviation(loc, trail.coordinates, 100);
        setDeviation(dev.isDeviated ? dev : null);
        if (dev.isDeviated) {
          dispatch({ type: 'SET_DEVIATION', payload: { detected: true, distance: dev.distance } });
        }
      }
    }, 5000);
    return () => LocationService.stopTracking();
  }, [state.currentHike.active]);

  // Safety check-in timer
  useEffect(() => {
    if (!state.currentHike.active) return;
    const interval = (state.user.preferences.checkInInterval || 60) * 60 * 1000;
    const timer = setInterval(() => setShowCheckIn(true), interval);
    return () => clearInterval(timer);
  }, [state.currentHike.active]);

  const handleCheckIn = () => {
    dispatch({ type: 'CHECK_IN' });
    setShowCheckIn(false);
  };

  const handleEndHike = () => {
    Alert.alert('End Hike', 'Are you sure you want to end this hike?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'End Hike',
        style: 'destructive',
        onPress: () => {
          const activity = {
            id: 'act-' + Date.now(),
            trailId: trail?.id,
            trailName: trail?.name || 'Unknown Trail',
            startTime: state.currentHike.startTime,
            endTime: new Date().toISOString(),
            distance: state.currentHike.distance / 1000,
            duration: elapsedTime / 60,
            elevationGain: state.currentHike.elevationGain,
            route: state.currentHike.route,
            checkIns: state.currentHike.checkIns,
          };
          dispatch({ type: 'ADD_ACTIVITY', payload: activity });
          dispatch({ type: 'END_HIKE' });
          LocationService.stopTracking();
          navigation.navigate('Activity', { activity, trail });
        },
      },
    ]);
  };

  const handlePause = () => {
    dispatch({ type: state.currentHike.paused ? 'RESUME_HIKE' : 'PAUSE_HIKE' });
  };

  const formatElapsed = (secs) => {
    const h = Math.floor(secs / 3600);
    const m = Math.floor((secs % 3600) / 60);
    const s = secs % 60;
    return (h > 0 ? h + ':' : '') + m.toString().padStart(2, '0') + ':' + s.toString().padStart(2, '0');
  };

  const mapRegion = trail ? {
    latitude: trail.coordinates[0].latitude,
    longitude: trail.coordinates[0].longitude,
    latitudeDelta: 0.03,
    longitudeDelta: 0.03,
  } : { latitude: 13.0, longitude: 75.5, latitudeDelta: 0.5, longitudeDelta: 0.5 };

  return (
    <View style={styles.container}>
      <MapView ref={mapRef} style={styles.map} initialRegion={mapRegion} showsUserLocation followsUserLocation>
        {trail && (
          <>
            <Polyline coordinates={trail.coordinates} strokeColor={colors.primary} strokeWidth={4} />
            <Marker coordinate={trail.startPoint} pinColor="green" />
            <Marker coordinate={trail.endPoint} pinColor="red" />
          </>
        )}
        {state.currentHike.route.length > 1 && (
          <Polyline
            coordinates={state.currentHike.route.map((r) => ({ latitude: r.latitude, longitude: r.longitude }))}
            strokeColor={colors.secondary}
            strokeWidth={3}
            lineDashPattern={[5, 5]}
          />
        )}
      </MapView>

      {/* Deviation warning */}
      {deviation && (
        <View style={styles.deviationBanner}>
          <Ionicons name="warning" size={20} color="#fff" />
          <Text style={styles.deviationText}>Off trail! {Math.round(deviation.distance)}m from path</Text>
        </View>
      )}

      {/* Top stats bar */}
      <View style={styles.topBar}>
        <View style={styles.topStat}>
          <Text style={styles.topStatValue}>{formatElapsed(elapsedTime)}</Text>
          <Text style={styles.topStatLabel}>Elapsed</Text>
        </View>
        <View style={styles.topStat}>
          <Text style={styles.topStatValue}>{(state.currentHike.distance / 1000).toFixed(2)} km</Text>
          <Text style={styles.topStatLabel}>Distance</Text>
        </View>
        <View style={styles.topStat}>
          <Text style={styles.topStatValue}>{Math.round(state.currentHike.elevationGain)}m</Text>
          <Text style={styles.topStatLabel}>Elevation</Text>
        </View>
      </View>

      {/* Bottom controls */}
      <View style={styles.bottomBar}>
        <View style={styles.controlsRow}>
          <TouchableOpacity style={styles.pauseBtn} onPress={handlePause}>
            <Ionicons name={state.currentHike.paused ? 'play' : 'pause'} size={28} color={colors.primary} />
          </TouchableOpacity>
          <TouchableOpacity style={styles.endBtn} onPress={handleEndHike}>
            <Ionicons name="stop" size={24} color="#fff" />
            <Text style={styles.endBtnText}>End Hike</Text>
          </TouchableOpacity>
          <EmergencyButton
            onPress={() => navigation.navigate('Safety', { screen: 'SOS' })}
            size="small"
            active={state.safetyStatus.sosActive}
          />
        </View>

        {/* Trail info */}
        {trail && (
          <View style={styles.trailInfo}>
            <Text style={styles.trailName}>{trail.name}</Text>
            <Text style={styles.trailMeta}>Remaining: ~{formatDuration(Math.max(0, trail.estimatedDuration - elapsedTime / 60))}</Text>
          </View>
        )}
      </View>

      {/* Safety check-in modal */}
      <SafetyCheckInModal
        visible={showCheckIn}
        onAcknowledge={handleCheckIn}
        onDismiss={() => { setShowCheckIn(false); navigation.navigate('Safety', { screen: 'SOS' }); }}
        timeRemaining={300}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1 },
  map: { flex: 1 },
  deviationBanner: { position: 'absolute', top: 50, alignSelf: 'center', flexDirection: 'row', backgroundColor: colors.danger, paddingHorizontal: 16, paddingVertical: 10, borderRadius: borderRadius.full, gap: 8, alignItems: 'center', ...shadows.medium },
  deviationText: { color: '#fff', fontWeight: '700', fontSize: 14 },
  topBar: { position: 'absolute', top: 50, left: spacing.md, right: spacing.md, flexDirection: 'row', backgroundColor: 'rgba(255,255,255,0.95)', borderRadius: borderRadius.lg, padding: spacing.sm, ...shadows.medium, justifyContent: 'space-around' },
  topStat: { alignItems: 'center' },
  topStatValue: { fontSize: 18, fontWeight: '700', color: colors.text },
  topStatLabel: { fontSize: 11, color: colors.textLight },
  bottomBar: { position: 'absolute', bottom: 0, left: 0, right: 0, backgroundColor: colors.surface, borderTopLeftRadius: borderRadius.xl, borderTopRightRadius: borderRadius.xl, padding: spacing.md, paddingBottom: spacing.xl, ...shadows.large },
  controlsRow: { flexDirection: 'row', justifyContent: 'space-around', alignItems: 'center', marginBottom: spacing.sm },
  pauseBtn: { width: 56, height: 56, borderRadius: 28, backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center', borderWidth: 2, borderColor: colors.primary },
  endBtn: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.danger, paddingHorizontal: 24, paddingVertical: 14, borderRadius: borderRadius.full, gap: 8 },
  endBtnText: { color: '#fff', fontWeight: '700', fontSize: 16 },
  trailInfo: { alignItems: 'center', paddingTop: spacing.sm, borderTopWidth: 1, borderTopColor: colors.border },
  trailName: { fontSize: 15, fontWeight: '600', color: colors.text },
  trailMeta: { fontSize: 12, color: colors.textLight },
});

export default NavigationScreen;
