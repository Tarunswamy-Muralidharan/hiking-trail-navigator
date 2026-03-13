import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';

const typeIcons = {
  wildlife: 'paw', fallen_tree: 'leaf', landslide: 'warning',
  terrain: 'alert-circle', flood: 'water', other: 'help-circle',
};

const severityColors = {
  low: '#4CAF50', medium: '#FF9800', high: '#F44336', critical: '#B71C1C',
};

const HazardCard = ({ hazard, onValidate, onPress }) => {
  const icon = typeIcons[hazard.type] || 'alert-circle';
  const sevColor = severityColors[hazard.severity] || colors.textLight;
  const timeAgo = getTimeAgo(hazard.timestamp || hazard.createdAt);

  return (
    <TouchableOpacity style={styles.card} onPress={() => onPress && onPress(hazard)} activeOpacity={0.8}>
      <View style={styles.row}>
        <View style={[styles.iconCircle, { backgroundColor: sevColor + '20' }]}>
          <Ionicons name={icon} size={24} color={sevColor} />
        </View>
        <View style={styles.info}>
          <View style={styles.titleRow}>
            <Text style={styles.type}>{(hazard.type || '').replace('_', ' ').toUpperCase()}</Text>
            <View style={[styles.sevBadge, { backgroundColor: sevColor }]}>
              <Text style={styles.sevText}>{hazard.severity}</Text>
            </View>
          </View>
          <Text style={styles.desc} numberOfLines={2}>{hazard.description}</Text>
          <View style={styles.metaRow}>
            <Text style={styles.meta}>{timeAgo}</Text>
            {hazard.verified && (
              <View style={styles.verifiedBadge}>
                <Ionicons name="checkmark-circle" size={12} color={colors.primary} />
                <Text style={styles.verifiedText}>Verified</Text>
              </View>
            )}
          </View>
        </View>
      </View>
      {onValidate && (
        <TouchableOpacity style={styles.confirmBtn} onPress={() => onValidate(hazard)}>
          <Ionicons name="thumbs-up" size={16} color={colors.primary} />
          <Text style={styles.confirmText}>Confirm ({hazard.confirmations || 0})</Text>
        </TouchableOpacity>
      )}
    </TouchableOpacity>
  );
};

const getTimeAgo = (timestamp) => {
  if (!timestamp) return 'Unknown';
  const diff = Date.now() - new Date(timestamp).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 60) return mins + 'm ago';
  const hours = Math.floor(mins / 60);
  if (hours < 24) return hours + 'h ago';
  return Math.floor(hours / 24) + 'd ago';
};

const styles = StyleSheet.create({
  card: { backgroundColor: colors.surface, borderRadius: borderRadius.lg, padding: spacing.md, marginVertical: spacing.xs, ...shadows.small },
  row: { flexDirection: 'row', gap: spacing.sm },
  iconCircle: { width: 48, height: 48, borderRadius: 24, justifyContent: 'center', alignItems: 'center' },
  info: { flex: 1 },
  titleRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 },
  type: { fontSize: 13, fontWeight: '700', color: colors.text },
  sevBadge: { paddingHorizontal: 8, paddingVertical: 2, borderRadius: borderRadius.full },
  sevText: { fontSize: 10, color: '#fff', fontWeight: '700', textTransform: 'uppercase' },
  desc: { fontSize: 13, color: colors.textLight, lineHeight: 18 },
  metaRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, marginTop: 4 },
  meta: { fontSize: 11, color: colors.textLight },
  verifiedBadge: { flexDirection: 'row', alignItems: 'center', gap: 2 },
  verifiedText: { fontSize: 11, color: colors.primary, fontWeight: '500' },
  confirmBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6, marginTop: spacing.sm, paddingTop: spacing.sm, borderTopWidth: 1, borderTopColor: colors.border },
  confirmText: { fontSize: 13, color: colors.primary, fontWeight: '600' },
});

export default React.memo(HazardCard);
