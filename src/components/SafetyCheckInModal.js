import React, { useEffect, useState } from 'react';
import { View, Text, Modal, TouchableOpacity, StyleSheet, Animated } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';

const SafetyCheckInModal = ({ visible, onAcknowledge, onDismiss, timeRemaining = 300, escalationLevel = 'warning' }) => {
  const [countdown, setCountdown] = useState(timeRemaining);
  const borderAnim = new Animated.Value(0);

  useEffect(() => {
    if (visible) {
      setCountdown(timeRemaining);
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Warning);
    }
  }, [visible]);

  useEffect(() => {
    if (!visible || countdown <= 0) return;
    const timer = setInterval(() => setCountdown((c) => c - 1), 1000);
    return () => clearInterval(timer);
  }, [visible, countdown]);

  const levelColors = {
    warning: colors.warning,
    alert: colors.secondary,
    emergency: colors.danger,
  };
  const borderColor = levelColors[escalationLevel] || colors.warning;
  const minutes = Math.floor(countdown / 60);
  const seconds = countdown % 60;

  return (
    <Modal visible={visible} transparent animationType="fade">
      <View style={styles.overlay}>
        <View style={[styles.card, { borderColor, borderWidth: 3 }]}>
          <View style={[styles.iconCircle, { backgroundColor: borderColor + '20' }]}>
            <Ionicons name="shield-checkmark" size={40} color={borderColor} />
          </View>
          <Text style={styles.title}>Safety Check-In</Text>
          <Text style={styles.message}>
            {escalationLevel === 'emergency'
              ? 'URGENT: Please confirm you are safe immediately!'
              : 'Are you safe? Please confirm your well-being.'}
          </Text>
          <View style={styles.timerBox}>
            <Text style={[styles.timer, { color: borderColor }]}>
              {minutes}:{seconds.toString().padStart(2, '0')}
            </Text>
            <Text style={styles.timerLabel}>Time remaining</Text>
          </View>
          <TouchableOpacity
            style={[styles.safeBtn, { backgroundColor: colors.primary }]}
            onPress={() => { Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success); onAcknowledge(); }}
          >
            <Ionicons name="checkmark-circle" size={24} color="#fff" />
            <Text style={styles.safeBtnText}>I'm Safe</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.helpBtn, { backgroundColor: colors.danger }]}
            onPress={onDismiss}
          >
            <Ionicons name="alert-circle" size={20} color="#fff" />
            <Text style={styles.helpBtnText}>Need Help</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.6)', justifyContent: 'center', alignItems: 'center', padding: spacing.lg },
  card: { backgroundColor: colors.surface, borderRadius: borderRadius.xl, padding: spacing.lg, width: '100%', alignItems: 'center', ...shadows.large },
  iconCircle: { width: 80, height: 80, borderRadius: 40, justifyContent: 'center', alignItems: 'center', marginBottom: spacing.md },
  title: { fontSize: 22, fontWeight: '700', color: colors.text, marginBottom: spacing.sm },
  message: { fontSize: 15, color: colors.textLight, textAlign: 'center', lineHeight: 22, marginBottom: spacing.md },
  timerBox: { alignItems: 'center', marginBottom: spacing.lg },
  timer: { fontSize: 36, fontWeight: '700' },
  timerLabel: { fontSize: 12, color: colors.textLight },
  safeBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', width: '100%', padding: spacing.md, borderRadius: borderRadius.lg, gap: 8, marginBottom: spacing.sm },
  safeBtnText: { color: '#fff', fontSize: 18, fontWeight: '700' },
  helpBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', width: '100%', padding: 12, borderRadius: borderRadius.lg, gap: 6 },
  helpBtnText: { color: '#fff', fontSize: 15, fontWeight: '600' },
});

export default SafetyCheckInModal;
