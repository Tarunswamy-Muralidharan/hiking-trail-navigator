import React, { memo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Circle, Marker, Callout } from 'react-native-maps';
import { Ionicons } from '@expo/vector-icons';
import { colors, typography, spacing, borderRadius } from '../constants/theme';

const ACTIVITY_LEVEL_CONFIG = {
  'very low': { fill: 'rgba(126, 87, 194, 0.25)', stroke: 'rgba(126, 87, 194, 0.6)', label: 'Very Low Activity' },
  low: { fill: 'rgba(126, 87, 194, 0.18)', stroke: 'rgba(126, 87, 194, 0.45)', label: 'Low Activity' },
  moderate: { fill: 'rgba(126, 87, 194, 0.10)', stroke: 'rgba(126, 87, 194, 0.30)', label: 'Moderate Activity' },
};

const UnexploredAreaOverlay = ({ areas = [], onAreaPress }) => {
  return (
    <>
      {areas.map((area, idx) => {
        const id = area.id || idx;
        const activityLevel = area.activityLevel || 'low';
        const config = ACTIVITY_LEVEL_CONFIG[activityLevel] || ACTIVITY_LEVEL_CONFIG.low;

        return (
          <React.Fragment key={`unexplored-${id}`}>
            <Circle
              center={area.center}
              radius={area.radius || 500}
              fillColor={config.fill}
              strokeColor={config.stroke}
              strokeWidth={2}
              lineDashPattern={[8, 6]}
              zIndex={0}
            />

            <Marker
              coordinate={area.center}
              anchor={{ x: 0.5, y: 0.5 }}
              onPress={() => onAreaPress && onAreaPress(area)}
              accessibilityLabel={`Unexplored area with ${activityLevel} activity`}
            >
              <View style={styles.cautionIcon}>
                <Ionicons name="eye-off-outline" size={18} color="#7E57C2" />
              </View>

              <Callout tooltip>
                <View style={styles.callout}>
                  <View style={styles.calloutHeader}>
                    <Ionicons name="alert-circle" size={18} color="#7E57C2" />
                    <Text style={styles.calloutTitle}>Unexplored Area</Text>
                  </View>

                  <Text style={styles.calloutMessage}>
                    Exercise increased caution in this area
                  </Text>

                  <View style={styles.activityRow}>
                    <Text style={styles.activityLabel}>Activity Level:</Text>
                    <View style={[styles.activityBadge, { backgroundColor: config.stroke }]}>
                      <Text style={styles.activityBadgeText}>
                        {config.label}
                      </Text>
                    </View>
                  </View>

                  <View style={styles.tipsContainer}>
                    <Text style={styles.tipsHeader}>Safety Tips:</Text>
                    <Text style={styles.tipText}>
                      {'\u2022'} Stay on marked trails{'\n'}
                      {'\u2022'} Inform someone of your plans{'\n'}
                      {'\u2022'} Carry extra supplies
                    </Text>
                  </View>
                </View>
              </Callout>
            </Marker>
          </React.Fragment>
        );
      })}
    </>
  );
};

const styles = StyleSheet.create({
  cautionIcon: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: 'rgba(237, 231, 246, 0.9)',
    borderWidth: 2,
    borderColor: '#7E57C2',
    alignItems: 'center',
    justifyContent: 'center',
  },
  callout: {
    backgroundColor: colors.surface,
    borderRadius: borderRadius.md,
    padding: spacing.md,
    minWidth: 220,
    maxWidth: 280,
    elevation: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 4,
  },
  calloutHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  calloutTitle: {
    fontSize: typography.h4,
    fontWeight: '700',
    color: '#5E35B1',
    marginLeft: spacing.xs,
  },
  calloutMessage: {
    fontSize: typography.body,
    color: colors.text,
    marginBottom: spacing.sm,
    fontStyle: 'italic',
  },
  activityRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  activityLabel: {
    fontSize: typography.caption,
    color: colors.textLight,
    marginRight: spacing.xs,
  },
  activityBadge: {
    paddingHorizontal: spacing.sm,
    paddingVertical: 2,
    borderRadius: borderRadius.sm,
  },
  activityBadgeText: {
    color: '#FFFFFF',
    fontSize: 11,
    fontWeight: '600',
  },
  tipsContainer: {
    backgroundColor: '#F3E5F5',
    borderRadius: borderRadius.sm,
    padding: spacing.sm,
  },
  tipsHeader: {
    fontSize: typography.caption,
    fontWeight: '600',
    color: '#5E35B1',
    marginBottom: spacing.xs,
  },
  tipText: {
    fontSize: typography.caption,
    color: colors.text,
    lineHeight: 18,
  },
});

export default memo(UnexploredAreaOverlay);
