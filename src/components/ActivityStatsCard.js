import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';

const StatBox = ({ icon, value, label, iconColor }) => (
  <View style={styles.statBox}>
    <Ionicons name={icon} size={22} color={iconColor || colors.primary} />
    <Text style={styles.statValue}>{value}</Text>
    <Text style={styles.statLabel}>{label}</Text>
  </View>
);

const ActivityStatsCard = ({ stats }) => {
  const formatDistance = (km) => km < 1 ? (km * 1000).toFixed(0) + 'm' : km.toFixed(1) + 'km';
  const formatTime = (min) => {
    const h = Math.floor(min / 60);
    const m = Math.round(min % 60);
    return h > 0 ? h + 'h ' + m + 'm' : m + 'min';
  };

  return (
    <View style={styles.container}>
      <View style={styles.row}>
        <StatBox icon="map-outline" value={formatDistance(stats.distance || 0)} label="Distance" />
        <StatBox icon="time-outline" value={formatTime(stats.duration || 0)} label="Duration" iconColor={colors.info} />
        <StatBox icon="trending-up" value={(stats.elevationGain || 0) + 'm'} label="Elevation" iconColor={colors.secondary} />
      </View>
      <View style={styles.row}>
        <StatBox icon="speedometer-outline" value={(stats.avgPace || 0).toFixed(1) + '/km'} label="Avg Pace" iconColor="#9C27B0" />
        <StatBox icon="triangle-outline" value={(stats.maxElevation || 0) + 'm'} label="Max Elev." iconColor={colors.primary} />
        <StatBox icon="flame-outline" value={(stats.calories || 0).toString()} label="Calories" iconColor={colors.danger} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { backgroundColor: colors.surface, borderRadius: borderRadius.lg, padding: spacing.md, ...shadows.medium },
  row: { flexDirection: 'row', justifyContent: 'space-around', marginVertical: spacing.sm },
  statBox: { alignItems: 'center', flex: 1 },
  statValue: { fontSize: 18, fontWeight: '700', color: colors.text, marginTop: 4 },
  statLabel: { fontSize: 11, color: colors.textLight, marginTop: 2 },
});

export default React.memo(ActivityStatsCard);
