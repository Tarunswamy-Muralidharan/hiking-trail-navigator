import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, TextInput, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import MapView, { Marker, Polyline, Circle } from 'react-native-maps';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { useAppContext } from '../store/AppContext';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';
import { trails } from '../constants/trailData';
import { dangerZones, noCoverageZones, unexploredAreas, getSeverityColor } from '../constants/dangerZones';
import EmergencyButton from '../components/EmergencyButton';
import WeatherAlert from '../components/WeatherAlert';
import NoNetworkWarning from '../components/NoNetworkWarning';
import * as LocationService from '../services/LocationService';

const INITIAL_REGION = {
  latitude: 13.0,
  longitude: 75.5,
  latitudeDelta: 2.5,
  longitudeDelta: 2.5,
};

const HomeScreen = () => {
  const navigation = useNavigation();
  const { state, dispatch } = useAppContext();
  const [searchQuery, setSearchQuery] = useState('');
  const [userLocation, setUserLocation] = useState(null);
  const [showLayers, setShowLayers] = useState({ dangers: true, noCoverage: true, unexplored: false, hazards: true });
  const [showNetworkWarning, setShowNetworkWarning] = useState(false);

  useEffect(() => {
    (async () => {
      const granted = await LocationService.requestPermissions();
      if (granted) {
        const loc = await LocationService.getCurrentLocation();
        if (loc) setUserLocation(loc);
      }
    })();
  }, []);

  const handleTrailPress = useCallback((trail) => {
    navigation.navigate('Trails', { screen: 'TrailDetail', params: { trail } });
  }, [navigation]);

  const handleSOS = useCallback(() => {
    navigation.navigate('Safety', { screen: 'SOS' });
  }, [navigation]);

  const toggleLayer = (layer) => {
    setShowLayers((prev) => ({ ...prev, [layer]: !prev[layer] }));
  };

  const getRiskColor = () => {
    switch (state.riskLevel) {
      case 'high': return colors.secondary;
      case 'critical': return colors.danger;
      case 'medium': return colors.warning;
      default: return colors.primary;
    }
  };

  return (
    <View style={styles.container}>
      <MapView style={styles.map} initialRegion={INITIAL_REGION} showsUserLocation showsMyLocationButton>
        {/* Trail polylines */}
        {trails.map((trail) => (
          <React.Fragment key={trail.id}>
            <Polyline
              coordinates={trail.coordinates}
              strokeColor={colors.primary}
              strokeWidth={3}
            />
            <Marker
              coordinate={trail.startPoint}
              title={trail.name}
              description={trail.difficulty + ' | ' + trail.distance + 'km'}
              onCalloutPress={() => handleTrailPress(trail)}
            >
              <View style={styles.trailMarker}>
                <Ionicons name="flag" size={16} color={colors.primary} />
              </View>
            </Marker>
          </React.Fragment>
        ))}

        {/* Danger zones */}
        {showLayers.dangers && dangerZones.map((zone) => (
          <Circle
            key={zone.id}
            center={zone.center}
            radius={zone.radius}
            fillColor={getSeverityColor(zone.severity)}
            strokeColor="rgba(244, 67, 54, 0.6)"
            strokeWidth={1}
          />
        ))}

        {/* No coverage zones */}
        {showLayers.noCoverage && noCoverageZones.map((zone) => (
          <Circle
            key={zone.id}
            center={zone.center}
            radius={zone.radius}
            fillColor="rgba(158, 158, 158, 0.25)"
            strokeColor="rgba(117, 117, 117, 0.5)"
            strokeWidth={1}
          />
        ))}

        {/* Unexplored areas */}
        {showLayers.unexplored && unexploredAreas.map((area) => (
          <Circle
            key={area.id}
            center={area.center}
            radius={area.radius}
            fillColor="rgba(156, 39, 176, 0.15)"
            strokeColor="rgba(156, 39, 176, 0.3)"
            strokeWidth={1}
          />
        ))}
      </MapView>

      {/* Search bar */}
      <SafeAreaView style={styles.searchContainer}>
        <View style={styles.searchBar}>
          <Ionicons name="search" size={20} color={colors.textLight} />
          <TextInput
            style={styles.searchInput}
            placeholder="Search trails..."
            placeholderTextColor={colors.textLight}
            value={searchQuery}
            onChangeText={setSearchQuery}
            onSubmitEditing={() => {
              if (searchQuery.trim()) {
                navigation.navigate('Trails', { screen: 'TrailDiscovery', params: { query: searchQuery } });
              }
            }}
          />
        </View>

        {/* Risk level badge */}
        <View style={[styles.riskBadge, { backgroundColor: getRiskColor() }]}>
          <Ionicons name="shield" size={14} color="#fff" />
          <Text style={styles.riskText}>{state.riskLevel.toUpperCase()}</Text>
        </View>
      </SafeAreaView>

      {/* Network status */}
      {!state.networkStatus.connected && (
        <View style={styles.offlineBanner}>
          <Ionicons name="cloud-offline" size={16} color="#fff" />
          <Text style={styles.offlineText}>Offline Mode</Text>
        </View>
      )}

      {/* Layer toggle buttons */}
      <View style={styles.layerControls}>
        <TouchableOpacity style={[styles.layerBtn, showLayers.dangers && styles.layerBtnActive]} onPress={() => toggleLayer('dangers')}>
          <Ionicons name="warning" size={18} color={showLayers.dangers ? '#fff' : colors.text} />
        </TouchableOpacity>
        <TouchableOpacity style={[styles.layerBtn, showLayers.noCoverage && styles.layerBtnActive]} onPress={() => toggleLayer('noCoverage')}>
          <Ionicons name="cellular-outline" size={18} color={showLayers.noCoverage ? '#fff' : colors.text} />
        </TouchableOpacity>
        <TouchableOpacity style={[styles.layerBtn, showLayers.unexplored && styles.layerBtnActive]} onPress={() => toggleLayer('unexplored')}>
          <Ionicons name="eye-off" size={18} color={showLayers.unexplored ? '#fff' : colors.text} />
        </TouchableOpacity>
      </View>

      {/* Weather alert */}
      {state.weather.alerts.length > 0 && (
        <View style={styles.weatherContainer}>
          <WeatherAlert alert={state.weather.alerts[0]} />
        </View>
      )}

      {/* No network warning */}
      <NoNetworkWarning
        visible={showNetworkWarning}
        onDismiss={() => setShowNetworkWarning(false)}
        onEnableOffline={() => { dispatch({ type: 'SET_OFFLINE', payload: true }); setShowNetworkWarning(false); }}
      />

      {/* SOS Button */}
      <View style={styles.sosContainer}>
        <EmergencyButton onPress={handleSOS} size="medium" active={state.safetyStatus.sosActive} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1 },
  map: { flex: 1 },
  searchContainer: { position: 'absolute', top: 0, left: 0, right: 0, flexDirection: 'row', paddingHorizontal: spacing.md, paddingTop: spacing.sm, gap: spacing.sm, alignItems: 'center' },
  searchBar: { flex: 1, flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, borderRadius: borderRadius.full, paddingHorizontal: spacing.md, height: 44, ...shadows.medium, gap: spacing.sm },
  searchInput: { flex: 1, fontSize: 15, color: colors.text },
  riskBadge: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 10, paddingVertical: 8, borderRadius: borderRadius.full, gap: 4, ...shadows.small },
  riskText: { color: '#fff', fontSize: 10, fontWeight: '800' },
  offlineBanner: { position: 'absolute', top: 100, alignSelf: 'center', flexDirection: 'row', backgroundColor: colors.textLight, paddingHorizontal: 12, paddingVertical: 6, borderRadius: borderRadius.full, gap: 6, alignItems: 'center' },
  offlineText: { color: '#fff', fontSize: 12, fontWeight: '600' },
  layerControls: { position: 'absolute', right: spacing.md, top: '35%', gap: spacing.sm },
  layerBtn: { width: 40, height: 40, borderRadius: 20, backgroundColor: colors.surface, justifyContent: 'center', alignItems: 'center', ...shadows.small },
  layerBtnActive: { backgroundColor: colors.primary },
  weatherContainer: { position: 'absolute', bottom: 100, left: 0, right: 0 },
  sosContainer: { position: 'absolute', bottom: spacing.lg, right: spacing.lg },
  trailMarker: { backgroundColor: '#fff', borderRadius: 12, padding: 4, ...shadows.small },
});

export default HomeScreen;
