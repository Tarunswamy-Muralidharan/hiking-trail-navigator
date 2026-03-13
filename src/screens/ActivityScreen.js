import React from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import MapView, { Polyline } from 'react-native-maps';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation, useRoute } from '@react-navigation/native';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';
import ActivityStatsCard from '../components/ActivityStatsCard';
import ElevationProfile from '../components/ElevationProfile';

const ActivityScreen = () => {
  const navigation = useNavigation();
  const route = useRoute();
  const activity = route.params?.activity;
  const trail = route.params?.trail;

  if (!activity) return <View style={styles.container}><Text>No activity data</Text></View>;

  const stats = {
    distance: activity.distance || 0,
    duration: activity.duration || 0,
    elevationGain: activity.elevationGain || 0,
    avgPace: activity.duration > 0 && activity.distance > 0 ? activity.duration / activity.distance : 0,
    maxElevation: trail?.elevationProfile ? Math.max(...trail.elevationProfile.map((e) => e.elevation)) : 0,
    calories: Math.round((activity.duration || 0) * 7.5),
  };

  const mapRegion = trail ? {
    latitude: trail.coordinates[Math.floor(trail.coordinates.length / 2)].latitude,
    longitude: trail.coordinates[Math.floor(trail.coordinates.length / 2)].longitude,
    latitudeDelta: 0.05,
    longitudeDelta: 0.05,
  } : null;

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Header */}
      <View style={styles.header}>
        <Ionicons name="checkmark-circle" size={48} color={colors.primary} />
        <Text style={styles.headerTitle}>Hike Complete!</Text>
        <Text style={styles.headerSub}>{activity.trailName}</Text>
        <Text style={styles.headerDate}>{new Date(activity.startTime).toLocaleDateString()}</Text>
      </View>

      {/* Stats */}
      <View style={styles.section}>
        <ActivityStatsCard stats={stats} />
      </View>

      {/* Map */}
      {mapRegion && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Route</Text>
          <View style={styles.mapBox}>
            <MapView style={styles.map} region={mapRegion} scrollEnabled={false}>
              {trail && <Polyline coordinates={trail.coordinates} strokeColor={colors.primary} strokeWidth={3} />}
              {activity.route && activity.route.length > 1 && (
                <Polyline
                  coordinates={activity.route.map((r) => ({ latitude: r.latitude, longitude: r.longitude }))}
                  strokeColor={colors.secondary}
                  strokeWidth={2}
                  lineDashPattern={[5, 5]}
                />
              )}
            </MapView>
          </View>
        </View>
      )}

      {/* Elevation */}
      {trail?.elevationProfile && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Elevation Profile</Text>
          <ElevationProfile elevationData={trail.elevationProfile} />
        </View>
      )}

      {/* Safety summary */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Safety Summary</Text>
        <View style={styles.safetyCard}>
          <View style={styles.safetyStat}>
            <Ionicons name="shield-checkmark" size={20} color={colors.primary} />
            <Text style={styles.safetyText}>{(activity.checkIns || []).length} check-ins completed</Text>
          </View>
          <View style={styles.safetyStat}>
            <Ionicons name="alert-circle" size={20} color={colors.primary} />
            <Text style={styles.safetyText}>No emergencies triggered</Text>
          </View>
        </View>
      </View>

      {/* Actions */}
      <View style={styles.actions}>
        <TouchableOpacity style={styles.historyBtn} onPress={() => navigation.navigate('ActivityHistory')}>
          <Ionicons name="list" size={20} color={colors.primary} />
          <Text style={styles.historyBtnText}>View All Activities</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.homeBtn} onPress={() => navigation.navigate('Home')}>
          <Text style={styles.homeBtnText}>Back to Home</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  header: { alignItems: 'center', paddingVertical: spacing.xl, backgroundColor: colors.surface },
  headerTitle: { fontSize: 24, fontWeight: '700', color: colors.primary, marginTop: spacing.sm },
  headerSub: { fontSize: 16, color: colors.text, fontWeight: '500', marginTop: 4 },
  headerDate: { fontSize: 13, color: colors.textLight, marginTop: 2 },
  section: { padding: spacing.md },
  sectionTitle: { fontSize: 18, fontWeight: '700', color: colors.text, marginBottom: spacing.sm },
  mapBox: { height: 200, borderRadius: borderRadius.lg, overflow: 'hidden', ...shadows.small },
  map: { flex: 1 },
  safetyCard: { backgroundColor: colors.surface, borderRadius: borderRadius.lg, padding: spacing.md, ...shadows.small },
  safetyStat: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, paddingVertical: 6 },
  safetyText: { fontSize: 14, color: colors.text },
  actions: { padding: spacing.md, gap: spacing.sm, paddingBottom: spacing.xxl },
  historyBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', backgroundColor: colors.surface, padding: 14, borderRadius: borderRadius.lg, borderWidth: 1, borderColor: colors.primary, gap: spacing.sm },
  historyBtnText: { color: colors.primary, fontSize: 15, fontWeight: '600' },
  homeBtn: { alignItems: 'center', padding: 14, borderRadius: borderRadius.lg, backgroundColor: colors.background },
  homeBtnText: { color: colors.textLight, fontSize: 14, fontWeight: '500' },
});

export default ActivityScreen;
