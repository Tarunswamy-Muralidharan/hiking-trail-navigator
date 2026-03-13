import React, { useEffect, useRef } from 'react';
import { TouchableOpacity, Text, StyleSheet, Animated } from 'react-native';
import * as Haptics from 'expo-haptics';
import { colors, shadows } from '../constants/theme';

const EmergencyButton = ({ onPress, size = 'medium', active = false, style }) => {
  const pulseAnim = useRef(new Animated.Value(1)).current;
  const sizes = { small: 48, medium: 72, large: 120 };
  const s = sizes[size] || sizes.medium;
  const fontSize = size === 'large' ? 24 : size === 'medium' ? 16 : 12;

  useEffect(() => {
    if (active) {
      Animated.loop(
        Animated.sequence([
          Animated.timing(pulseAnim, { toValue: 1.15, duration: 600, useNativeDriver: true }),
          Animated.timing(pulseAnim, { toValue: 1, duration: 600, useNativeDriver: true }),
        ])
      ).start();
    } else {
      pulseAnim.setValue(1);
    }
  }, [active]);

  const handlePress = () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Heavy);
    onPress();
  };

  return (
    <Animated.View style={[{ transform: [{ scale: pulseAnim }] }, style]}>
      <TouchableOpacity
        onPress={handlePress}
        style={[styles.button, { width: s, height: s, borderRadius: s / 2 }, active && styles.active]}
        activeOpacity={0.8}
        accessibilityLabel="Emergency SOS Button"
        accessibilityRole="button"
      >
        <Text style={[styles.text, { fontSize }]}>SOS</Text>
      </TouchableOpacity>
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  button: {
    backgroundColor: colors.danger,
    justifyContent: 'center',
    alignItems: 'center',
    ...shadows.large,
  },
  active: { backgroundColor: '#B71C1C' },
  text: { color: '#fff', fontWeight: '900', letterSpacing: 2 },
});

export default React.memo(EmergencyButton);
