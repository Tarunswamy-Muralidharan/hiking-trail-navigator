import React, { useState, useMemo } from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import MapView, { Polyline, Marker } from 'react-native-maps';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation, useRoute } from '@react-navigation/native';
import { useAppContext } from '../store/AppContext';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';
import { getDifficultyColor, formatDuration } from '../constants/trailData';
import ElevationProfile from '../components/ElevationProfile';
import HazardCard from '../components/HazardCard';
import { calculateTrailRisk, getRiskLevel, getRiskFactors } from '../services/RiskAssessmentService';

const TrailDetailScreen = () => {
  const navigation = useNavigation();
  const route = useRoute();
  const { state, dispatch } = useAppContext();
  const trail = route.params?.trail;
  const [showFullDesc, setShowFullDesc] = useState(false);

  if (!trail) return <View style={styles.container}><Text>Trail not found</Text></View>;

  const diffColor = getDifficultyColor(trail.difficulty);
  const riskScore = calculateTrailRisk(trail, state.weather, [], [], state.networkStatus);
  const riskLevel = getRiskLevel(riskScore);
  const riskFactors = getRiskFactors(trail, state.weather);

  const riskColors = { low: colors.primary, medium: colors.warning, high: colors.secondary, critical: colors.danger };
  const mapRegion = {
    latitude: trail.coordinates[Math.floor(trail.coordinates.length / 2)].latitude,
    longitude: trail.coordinates[Math.floor(trail.coordinates.length / 2)].longitude,
    latitudeDelta: 0.05,
    longitudeDelta: 0.05,
  };

  const handleStartHike = () => {
    Alert.alert('Start Hike', 'Begin hiking ' + trail.name + '?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Start',
        onPress: () => {
          dispatch({ type: 'START_HIKE', payload: { trailId: trail.id, trailName: trail.name } });
          navigation.navigate('Navigate', { screen: 'NavigationMain', params: { trail } });
        },
      },
    ]);
  };

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Map header */}
      <View style={styles.mapContainer}>
        <MapView style={styles.map} region={mapRegion} scrollEnabled={false} zoomEnabled={false}>
          <Polyline coordinates={trail.coordinates} strokeColor={colors.primary} strokeWidth={4} />
          <Marker coordinate={trail.startPoint} pinColor="green" title="Start" />
          <Marker coordinate={trail.endPoint} pinColor="red" title="End" />
        </MapView>
      </View>

      <View style={styles.content}>
        {/* Title section */}
        <View style={styles.titleSection}>
          <View style={styles.titleRow}>
            <Text style={styles.name}>{trail.name}</Text>
            <View style={[styles.diffBadge, { backgroundColor: diffColor }]}>
              <Text style={styles.diffText}>{trail.difficulty}</Text>
            </View>
          </View>
          <View style={styles.ratingRow}>
            {[1, 2, 3, 4, 5].map((i) => (
              <Ionicons key={i} name={i <= Math.floor(trail.rating) ? 'star' : i - 0.5 <= trail.rating ? 'star-half' : 'star-outline'} size={18} color="#FFB300" />
            ))}
            <Text style={styles.ratingNum}>{trail.rating}</Text>
          </View>
        </View>

        {/* Stats row */}
        <View style={styles.statsRow}>
          <View style={styles.statBox}>
            <Ionicons name="map-outline" size={20} color={colors.primary} />
            <Text style={styles.statValue}>{trail.distance} km</Text>
            <Text style={styles.statLabel}>Distance</Text>
          </View>
          <View style={styles.statBox}>
            <Ionicons name="time-outline" size={20} color={colors.info} />
            <Text style={styles.statValue}>{formatDuration(trail.estimatedDuration)}</Text>
            <Text style={styles.statLabel}>Duration</Text>
          </View>
          <View style={styles.statBox}>
            <Ionicons name="trending-up" size={20} color={colors.secondary} />
            <Text style={styles.statValue}>{trail.elevationGain}m</Text>
            <Text style={styles.statLabel}>Elevation</Text>
          </View>
          <View style={styles.statBox}>
            <Ionicons name="people-outline" size={20} color="#9C27B0" />
            <Text style={styles.statValue}>{trail.popularity}%</Text>
            <Text style={styles.statLabel}>Popular</Text>
          </View>
        </View>

        {/* Description */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>About this trail</Text>
          <Text style={styles.description} numberOfLines={showFullDesc ? undefined : 3}>{trail.description}</Text>
          <TouchableOpacity onPress={() => setShowFullDesc(!showFullDesc)}>
            <Text style={styles.readMore}>{showFullDesc ? 'Show less' : 'Read more'}</Text>
          </TouchableOpacity>
        </View>

        {/* Elevation Profile */}
        {trail.elevationProfile && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Elevation Profile</Text>
            <ElevationProfile elevationData={trail.elevationProfile} />
          </View>
        )}

        {/* Risk Assessment */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Risk Assessment</Text>
          <View style={[styles.riskCard, { borderLeftColor: riskColors[riskLevel], borderLeftWidth: 4 }]}>
            <View style={styles.riskHeader}>
              <Ionicons name="shield" size={20} color={riskColors[riskLevel]} />
              <Text style={[styles.riskLevel, { color: riskColors[riskLevel] }]}>{riskLevel.toUpperCase()} RISK</Text>
              <Text style={styles.riskScore}>Score: {riskScore}/100</Text>
            </View>
            {riskFactors.map((f, i) => (
              <View key={i} style={styles.riskFactor}>
                <Ionicons name="alert-circle" size={14} color={colors.textLight} />
                <Text style={styles.riskFactorText}>{f.label}</Text>
              </View>
            ))}
          </View>
        </View>

        {/* Hazards */}
        {trail.hazards.length > 0 && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Known Hazards</Text>
            {trail.hazards.map((h, i) => (
              <View key={i} style={styles.hazardItem}>
                <Ionicons name="warning" size={16} color={colors.warning} />
                <Text style={styles.hazardText}>{h}</Text>
              </View>
            ))}
          </View>
        )}

        {/* Coverage status */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Network Coverage</Text>
          <View style={styles.coverageRow}>
            <Ionicons
              name={trail.coverageStatus === 'full' ? 'cellular' : trail.coverageStatus === 'partial' ? 'cellular-outline' : 'close-circle'}
              size={20}
              color={trail.coverageStatus === 'full' ? colors.primary : trail.coverageStatus === 'partial' ? colors.warning : colors.danger}
            />
            <Text style={styles.coverageText}>
              {trail.coverageStatus === 'full' ? 'Full coverage available' : trail.coverageStatus === 'partial' ? 'Partial coverage - some dead zones' : 'No coverage - offline mode recommended'}
            </Text>
          </View>
        </View>

        {/* Actions */}
        <View style={styles.actions}>
          <TouchableOpacity style={styles.startBtn} onPress={handleStartHike}>
            <Ionicons name="navigate" size={22} color="#fff" />
            <Text style={styles.startBtnText}>Start Hike</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.downloadBtn} onPress={() => Alert.alert('Offline', 'Trail data saved for offline use.')}>
            <Ionicons name="download-outline" size={20} color={colors.primary} />
            <Text style={styles.downloadBtnText}>Save Offline</Text>
          </TouchableOpacity>
        </View>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  mapContainer: { height: 220 },
  map: { flex: 1 },
  content: { padding: spacing.md },
  titleSection: { marginBottom: spacing.md },
  titleRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  name: { fontSize: 24, fontWeight: '700', color: colors.text, flex: 1, marginRight: spacing.sm },
  diffBadge: { paddingHorizontal: 14, paddingVertical: 5, borderRadius: borderRadius.full },
  diffText: { color: '#fff', fontSize: 12, fontWeight: '700' },
  ratingRow: { flexDirection: 'row', alignItems: 'center', gap: 2, marginTop: spacing.xs },
  ratingNum: { fontSize: 14, fontWeight: '600', color: colors.text, marginLeft: spacing.sm },
  statsRow: { flexDirection: 'row', justifyContent: 'space-around', backgroundColor: colors.surface, borderRadius: borderRadius.lg, padding: spacing.md, ...shadows.small, marginBottom: spacing.md },
  statBox: { alignItems: 'center' },
  statValue: { fontSize: 16, fontWeight: '700', color: colors.text, marginTop: 4 },
  statLabel: { fontSize: 11, color: colors.textLight, marginTop: 2 },
  section: { marginBottom: spacing.lg },
  sectionTitle: { fontSize: 18, fontWeight: '700', color: colors.text, marginBottom: spacing.sm },
  description: { fontSize: 14, color: colors.textLight, lineHeight: 22 },
  readMore: { color: colors.primary, fontWeight: '600', marginTop: 4, fontSize: 13 },
  riskCard: { backgroundColor: colors.surface, borderRadius: borderRadius.md, padding: spacing.md, ...shadows.small },
  riskHeader: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, marginBottom: spacing.sm },
  riskLevel: { fontSize: 15, fontWeight: '700', flex: 1 },
  riskScore: { fontSize: 12, color: colors.textLight },
  riskFactor: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, paddingVertical: 4 },
  riskFactorText: { fontSize: 13, color: colors.textLight },
  hazardItem: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, paddingVertical: 6 },
  hazardText: { fontSize: 14, color: colors.text },
  coverageRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  coverageText: { fontSize: 14, color: colors.text },
  actions: { gap: spacing.sm, marginBottom: spacing.xl },
  startBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', backgroundColor: colors.primary, padding: spacing.md, borderRadius: borderRadius.lg, gap: spacing.sm },
  startBtnText: { color: '#fff', fontSize: 18, fontWeight: '700' },
  downloadBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', backgroundColor: colors.surface, padding: 14, borderRadius: borderRadius.lg, borderWidth: 1, borderColor: colors.primary, gap: spacing.sm },
  downloadBtnText: { color: colors.primary, fontSize: 15, fontWeight: '600' },
});

export default TrailDetailScreen;
