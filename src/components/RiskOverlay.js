import React, { memo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Circle, Polygon, Marker } from 'react-native-maps';
import { Ionicons } from '@expo/vector-icons';
import DangerZoneMarker from './DangerZoneMarker';
import { colors, typography, spacing, borderRadius } from '../constants/theme';

const HAZARD_ICONS = {
  wildlife: 'paw',
  tree: 'leaf',
  landslide: 'warning',
  terrain: 'alert-circle',
  flood: 'water',
  rockfall: 'cube',
  default: 'alert',
};

const RiskOverlay = ({
  dangerZones = [],
  noCoverageZones = [],
  unexploredAreas = [],
  hazards = [],
  visibleLayers = {
    dangerZones: true,
    noCoverage: true,
    unexplored: true,
    hazards: true,
  },
}) => {
  return (
    <>
      {/* Danger zone layer */}
      {visibleLayers.dangerZones &&
        dangerZones.map((zone) => (
          <DangerZoneMarker
            key={`dz-${zone.id}`}
            dangerZone={zone}
            onPress={zone.onPress}
          />
        ))}

      {/* No coverage zones layer */}
      {visibleLayers.noCoverage &&
        noCoverageZones.map((zone) => (
          <React.Fragment key={`nc-${zone.id}`}>
            <Circle
              center={zone.center}
              radius={zone.radius || 500}
              fillColor="rgba(158, 158, 158, 0.30)"
              strokeColor="#9E9E9E"
              strokeWidth={2}
              lineDashPattern={[10, 5]}
              zIndex={0}
            />
            <Marker coordinate={zone.center} anchor={{ x: 0.5, y: 0.5 }}>
              <View style={styles.noCoverageIcon}>
                <Ionicons name="cellular-outline" size={16} color="#757575" />
                <View style={styles.noCoverageSlash} />
              </View>
            </Marker>
          </React.Fragment>
        ))}

      {/* Unexplored areas layer */}
      {visibleLayers.unexplored &&
        unexploredAreas.map((area) => {
          if (area.polygon) {
            return (
              <Polygon
                key={`ua-${area.id}`}
                coordinates={area.polygon}
                fillColor="rgba(126, 87, 194, 0.18)"
                strokeColor="rgba(126, 87, 194, 0.5)"
                strokeWidth={1}
                zIndex={0}
              />
            );
          }
          return (
            <React.Fragment key={`ua-${area.id}`}>
              <Circle
                center={area.center}
                radius={area.radius || 400}
                fillColor="rgba(126, 87, 194, 0.18)"
                strokeColor="rgba(126, 87, 194, 0.5)"
                strokeWidth={1}
                lineDashPattern={[6, 4]}
                zIndex={0}
              />
              <Marker coordinate={area.center} anchor={{ x: 0.5, y: 0.5 }}>
                <View style={styles.unexploredIcon}>
                  <Ionicons name="help-circle" size={18} color="#7E57C2" />
                </View>
              </Marker>
            </React.Fragment>
          );
        })}

      {/* Hazard markers layer */}
      {visibleLayers.hazards &&
        hazards.map((hazard) => {
          const iconName = HAZARD_ICONS[hazard.type] || HAZARD_ICONS.default;
          return (
            <Marker
              key={`hz-${hazard.id}`}
              coordinate={hazard.coordinate}
              onPress={() => hazard.onPress && hazard.onPress(hazard)}
              accessibilityLabel={`Hazard: ${hazard.type}`}
            >
              <View style={styles.hazardMarker}>
                <Ionicons name={iconName} size={16} color={colors.danger} />
              </View>
            </Marker>
          );
        })}
    </>
  );
};

/** Standalone legend component for use outside the map */
export const RiskLegend = ({ visibleLayers = {} }) => {
  const items = [];

  if (visibleLayers.dangerZones) {
    items.push({ color: colors.danger, label: 'Danger Zone' });
  }
  if (visibleLayers.noCoverage) {
    items.push({ color: '#9E9E9E', label: 'No Coverage' });
  }
  if (visibleLayers.unexplored) {
    items.push({ color: '#7E57C2', label: 'Unexplored Area' });
  }
  if (visibleLayers.hazards) {
    items.push({ color: colors.secondary, label: 'Hazard Report' });
  }

  if (items.length === 0) return null;

  return (
    <View style={styles.legend} accessibilityRole="summary">
      <Text style={styles.legendTitle}>Map Legend</Text>
      {items.map((item) => (
        <View key={item.label} style={styles.legendItem}>
          <View style={[styles.legendDot, { backgroundColor: item.color }]} />
          <Text style={styles.legendLabel}>{item.label}</Text>
        </View>
      ))}
    </View>
  );
};

const styles = StyleSheet.create({
  noCoverageIcon: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: 'rgba(224,224,224,0.85)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  noCoverageSlash: {
    position: 'absolute',
    width: 2,
    height: 20,
    backgroundColor: '#757575',
    transform: [{ rotate: '45deg' }],
  },
  unexploredIcon: {
    width: 26,
    height: 26,
    borderRadius: 13,
    backgroundColor: 'rgba(237,231,246,0.85)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  hazardMarker: {
    width: 30,
    height: 30,
    borderRadius: 15,
    backgroundColor: '#FFEBEE',
    borderWidth: 2,
    borderColor: colors.danger,
    alignItems: 'center',
    justifyContent: 'center',
  },
  legend: {
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderRadius: borderRadius.md,
    padding: spacing.sm,
    position: 'absolute',
    bottom: spacing.md,
    left: spacing.md,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
  },
  legendTitle: {
    fontSize: typography.caption,
    fontWeight: '700',
    color: colors.text,
    marginBottom: spacing.xs,
  },
  legendItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 3,
  },
  legendDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    marginRight: spacing.sm,
  },
  legendLabel: {
    fontSize: 11,
    color: colors.textLight,
  },
});

export default memo(RiskOverlay);
