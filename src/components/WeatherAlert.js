import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Animated } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, spacing, borderRadius } from '../constants/theme';

const severityBg = {
  info: colors.info, warning: colors.warning, severe: colors.secondary, critical: colors.danger,
};
const weatherIcons = {
  clear: 'sunny', cloudy: 'cloud', rain: 'rainy', storm: 'thunderstorm',
  snow: 'snow', wind: 'flag', fog: 'cloudy-night', default: 'cloud',
};

const WeatherAlert = ({ alert, onDismiss }) => {
  if (!alert) return null;
  const bg = severityBg[alert.severity] || colors.info;
  const icon = weatherIcons[alert.type] || weatherIcons.default;

  return (
    <View style={[styles.container, { backgroundColor: bg }]}>
      <Ionicons name={icon} size={22} color="#fff" />
      <View style={styles.content}>
        {alert.temperature !== undefined && (
          <Text style={styles.temp}>{alert.temperature}C</Text>
        )}
        <Text style={styles.message} numberOfLines={2}>{alert.message}</Text>
      </View>
      {onDismiss && (
        <TouchableOpacity onPress={onDismiss} style={styles.close}>
          <Ionicons name="close" size={20} color="#fff" />
        </TouchableOpacity>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row', alignItems: 'center', padding: spacing.sm,
    borderRadius: borderRadius.md, marginHorizontal: spacing.md, marginVertical: spacing.xs, gap: spacing.sm,
  },
  content: { flex: 1 },
  temp: { color: '#fff', fontSize: 14, fontWeight: '700' },
  message: { color: '#fff', fontSize: 12, lineHeight: 16 },
  close: { padding: 4 },
});

export default React.memo(WeatherAlert);
