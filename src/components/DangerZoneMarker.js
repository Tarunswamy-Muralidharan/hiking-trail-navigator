import React, { memo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Marker, Circle, Callout } from 'react-native-maps';
import { Ionicons } from '@expo/vector-icons';
import { colors, typography, spacing, borderRadius } from '../constants/theme';

const SEVERITY_COLORS = {
  low: { fill: 'rgba(249, 168, 37, 0.20)', stroke: '#F9A825' },
  medium: { fill: 'rgba(255, 111, 0, 0.25)', stroke: '#FF6F00' },
  high: { fill: 'rgba(211, 47, 47, 0.30)', stroke: '#D32F2F' },
  critical: { fill: 'rgba(136, 14, 14, 0.35)', stroke: '#880E0E' },
};

const DangerZoneMarker = ({ dangerZone, onPress }) => {
  if (!dangerZone) return null;

  const {
    id,
    name,
    center,
    radius = 200,
    type,
    severity = 'medium',
    description,
    source,
    reportCount,
  } = dangerZone;

  const severityStyle = SEVERITY_COLORS[severity] || SEVERITY_COLORS.medium;

  return (
    <>
      <Circle
        key={`circle-${id}`}
        center={center}
        radius={radius}
        fillColor={severityStyle.fill}
        strokeColor={severityStyle.stroke}
        strokeWidth={2}
        zIndex={1}
      />
      <Marker
        key={`marker-${id}`}
        coordinate={center}
        onPress={() => onPress && onPress(dangerZone)}
        accessibilityLabel={`Danger zone: ${name}, severity ${severity}`}
      >
        <View style={[styles.markerIcon, { backgroundColor: severityStyle.stroke }]}>
          <Ionicons name="warning" size={18} color="#FFFFFF" />
        </View>

        <Callout tooltip onPress={() => onPress && onPress(dangerZone)}>
          <View style={styles.callout}>
            <Text style={styles.calloutTitle}>{name || 'Danger Zone'}</Text>

            <View style={styles.calloutRow}>
              <Text style={styles.calloutLabel}>Type:</Text>
              <Text style={styles.calloutValue}>{type || 'Unknown'}</Text>
            </View>

            <View style={styles.calloutRow}>
              <Text style={styles.calloutLabel}>Severity:</Text>
              <View style={[styles.severityBadge, { backgroundColor: severityStyle.stroke }]}>
                <Text style={styles.severityText}>
                  {severity.charAt(0).toUpperCase() + severity.slice(1)}
                </Text>
              </View>
            </View>

            {description ? (
              <Text style={styles.calloutDesc} numberOfLines={3}>
                {description}
              </Text>
            ) : null}

            <View style={styles.calloutFooter}>
              {source ? (
                <View style={styles.sourceTag}>
                  <Ionicons
                    name={source === 'authority' ? 'shield-checkmark' : 'people'}
                    size={12}
                    color={colors.textLight}
                  />
                  <Text style={styles.sourceText}>
                    {source === 'authority' ? 'Official' : 'Community'}
                  </Text>
                </View>
              ) : null}

              {reportCount != null && (
                <Text style={styles.reportCount}>
                  {reportCount} report{reportCount !== 1 ? 's' : ''}
                </Text>
              )}
            </View>
          </View>
        </Callout>
      </Marker>
    </>
  );
};

const styles = StyleSheet.create({
  markerIcon: {
    width: 32,
    height: 32,
    borderRadius: borderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: '#FFFFFF',
  },
  callout: {
    backgroundColor: colors.surface,
    borderRadius: borderRadius.md,
    padding: spacing.md,
    minWidth: 200,
    maxWidth: 280,
    elevation: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 4,
  },
  calloutTitle: {
    fontSize: typography.h4,
    fontWeight: '700',
    color: colors.text,
    marginBottom: spacing.sm,
  },
  calloutRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.xs,
  },
  calloutLabel: {
    fontSize: typography.caption,
    color: colors.textLight,
    marginRight: spacing.xs,
    fontWeight: '600',
  },
  calloutValue: {
    fontSize: typography.caption,
    color: colors.text,
  },
  severityBadge: {
    paddingHorizontal: spacing.sm,
    paddingVertical: 2,
    borderRadius: borderRadius.sm,
  },
  severityText: {
    color: '#FFFFFF',
    fontSize: 11,
    fontWeight: '600',
  },
  calloutDesc: {
    fontSize: typography.body,
    color: colors.text,
    marginTop: spacing.xs,
    marginBottom: spacing.sm,
    lineHeight: 20,
  },
  calloutFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: '#E0E0E0',
    paddingTop: spacing.xs,
    marginTop: spacing.xs,
  },
  sourceTag: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  sourceText: {
    fontSize: typography.caption,
    color: colors.textLight,
    marginLeft: spacing.xs,
  },
  reportCount: {
    fontSize: typography.caption,
    color: colors.textLight,
  },
});

export default memo(DangerZoneMarker);
