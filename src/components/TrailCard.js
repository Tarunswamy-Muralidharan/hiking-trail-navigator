import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';
import { getDifficultyColor, formatDuration } from '../constants/trailData';

const TrailCard = ({ trail, onPress, compact = false }) => {
  const diffColor = getDifficultyColor(trail.difficulty);

  if (compact) {
    return (
      <TouchableOpacity style={styles.compactCard} onPress={() => onPress(trail)} activeOpacity={0.7}>
        <View style={[styles.diffBadge, { backgroundColor: diffColor }]}>
          <Text style={styles.diffText}>{trail.difficulty}</Text>
        </View>
        <View style={styles.compactInfo}>
          <Text style={styles.compactName} numberOfLines={1}>{trail.name}</Text>
          <Text style={styles.compactStats}>{trail.distance}km | {formatDuration(trail.estimatedDuration)}</Text>
        </View>
        <View style={styles.ratingBox}>
          <Ionicons name="star" size={14} color="#FFB300" />
          <Text style={styles.ratingText}>{trail.rating}</Text>
        </View>
      </TouchableOpacity>
    );
  }

  return (
    <TouchableOpacity style={styles.card} onPress={() => onPress(trail)} activeOpacity={0.7}>
      <View style={styles.header}>
        <View style={styles.titleRow}>
          <Text style={styles.name} numberOfLines={1}>{trail.name}</Text>
          <View style={[styles.diffBadge, { backgroundColor: diffColor }]}>
            <Text style={styles.diffText}>{trail.difficulty}</Text>
          </View>
        </View>
        <Text style={styles.region}>{trail.region}</Text>
      </View>
      <Text style={styles.description} numberOfLines={2}>{trail.description}</Text>
      <View style={styles.statsRow}>
        <View style={styles.stat}>
          <Ionicons name="map-outline" size={16} color={colors.textLight} />
          <Text style={styles.statText}>{trail.distance} km</Text>
        </View>
        <View style={styles.stat}>
          <Ionicons name="time-outline" size={16} color={colors.textLight} />
          <Text style={styles.statText}>{formatDuration(trail.estimatedDuration)}</Text>
        </View>
        <View style={styles.stat}>
          <Ionicons name="trending-up" size={16} color={colors.textLight} />
          <Text style={styles.statText}>{trail.elevationGain}m</Text>
        </View>
        <View style={styles.stat}>
          <Ionicons name="star" size={16} color="#FFB300" />
          <Text style={styles.statText}>{trail.rating}</Text>
        </View>
      </View>
      {trail.hazards.length > 0 && (
        <View style={styles.hazardRow}>
          <Ionicons name="warning" size={14} color={colors.warning} />
          <Text style={styles.hazardText}>{trail.hazards.length} known hazard{trail.hazards.length > 1 ? 's' : ''}</Text>
        </View>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.surface,
    borderRadius: borderRadius.lg,
    padding: spacing.md,
    marginHorizontal: spacing.md,
    marginVertical: spacing.sm,
    ...shadows.medium,
  },
  header: { marginBottom: spacing.sm },
  titleRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  name: { fontSize: 18, fontWeight: '700', color: colors.text, flex: 1, marginRight: spacing.sm },
  region: { fontSize: 12, color: colors.textLight, marginTop: 2 },
  diffBadge: { paddingHorizontal: 10, paddingVertical: 3, borderRadius: borderRadius.full },
  diffText: { color: '#fff', fontSize: 11, fontWeight: '700' },
  description: { fontSize: 13, color: colors.textLight, lineHeight: 18, marginBottom: spacing.sm },
  statsRow: { flexDirection: 'row', justifyContent: 'space-between', paddingTop: spacing.sm, borderTopWidth: 1, borderTopColor: colors.border },
  stat: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  statText: { fontSize: 13, color: colors.text, fontWeight: '500' },
  hazardRow: { flexDirection: 'row', alignItems: 'center', gap: 4, marginTop: spacing.sm, paddingTop: spacing.xs, borderTopWidth: 1, borderTopColor: colors.border },
  hazardText: { fontSize: 12, color: colors.warning, fontWeight: '500' },
  compactCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: spacing.sm, marginHorizontal: spacing.md, marginVertical: 4, borderRadius: borderRadius.md, ...shadows.small },
  compactInfo: { flex: 1, marginLeft: spacing.sm },
  compactName: { fontSize: 14, fontWeight: '600', color: colors.text },
  compactStats: { fontSize: 12, color: colors.textLight },
  ratingBox: { flexDirection: 'row', alignItems: 'center', gap: 2 },
  ratingText: { fontSize: 13, fontWeight: '600', color: colors.text },
});

export default React.memo(TrailCard);
