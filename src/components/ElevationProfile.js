import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { colors, spacing, borderRadius } from '../constants/theme';

const ElevationProfile = ({ elevationData = [], currentPosition, height = 120 }) => {
  if (elevationData.length < 2) return null;

  const maxElev = Math.max(...elevationData.map((d) => d.elevation));
  const minElev = Math.min(...elevationData.map((d) => d.elevation));
  const maxDist = Math.max(...elevationData.map((d) => d.distance));
  const range = maxElev - minElev || 1;

  return (
    <View style={styles.container}>
      <View style={styles.labels}>
        <Text style={styles.label}>{maxElev}m</Text>
        <Text style={styles.label}>{minElev}m</Text>
      </View>
      <View style={[styles.chart, { height }]}>
        {elevationData.map((point, i) => {
          const barHeight = ((point.elevation - minElev) / range) * (height - 20);
          const leftPct = (point.distance / maxDist) * 100;
          const isActive = currentPosition && Math.abs(point.distance - currentPosition) < 0.5;
          return (
            <View
              key={i}
              style={[
                styles.bar,
                {
                  height: barHeight,
                  left: leftPct + '%',
                  backgroundColor: isActive ? colors.secondary : colors.primary,
                  width: Math.max(100 / elevationData.length - 2, 8),
                },
              ]}
            />
          );
        })}
      </View>
      <View style={styles.distLabels}>
        <Text style={styles.label}>0km</Text>
        <Text style={styles.label}>{maxDist}km</Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { marginVertical: spacing.sm },
  labels: { flexDirection: 'column', justifyContent: 'space-between', position: 'absolute', left: 0, top: 0, bottom: 20, zIndex: 1 },
  label: { fontSize: 10, color: colors.textLight },
  chart: { marginLeft: 35, backgroundColor: colors.background, borderRadius: borderRadius.sm, flexDirection: 'row', alignItems: 'flex-end', position: 'relative', overflow: 'hidden' },
  bar: { position: 'absolute', bottom: 0, borderTopLeftRadius: 2, borderTopRightRadius: 2, opacity: 0.8 },
  distLabels: { flexDirection: 'row', justifyContent: 'space-between', marginLeft: 35, marginTop: 2 },
});

export default React.memo(ElevationProfile);
