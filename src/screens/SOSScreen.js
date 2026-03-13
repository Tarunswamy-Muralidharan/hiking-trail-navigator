import React, { useState, useRef, useCallback } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert, Animated, Linking } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import * as Haptics from 'expo-haptics';
import { useAppContext } from '../store/AppContext';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';
import * as EmergencyService from '../services/EmergencyService';
import * as LocationService from '../services/LocationService';

const SOSScreen = () => {
  const navigation = useNavigation();
  const { state, dispatch } = useAppContext();
  const [holdProgress] = useState(new Animated.Value(0));
  const [isHolding, setIsHolding] = useState(false);
  const holdTimer = useRef(null);
  const pulseAnim = useRef(new Animated.Value(1)).current;

  const isSOSActive = state.safetyStatus.sosActive;

  React.useEffect(() => {
    if (isSOSActive) {
      Animated.loop(
        Animated.sequence([
          Animated.timing(pulseAnim, { toValue: 1.1, duration: 800, useNativeDriver: true }),
          Animated.timing(pulseAnim, { toValue: 1, duration: 800, useNativeDriver: true }),
        ])
      ).start();
    }
  }, [isSOSActive]);

  const startHold = () => {
    setIsHolding(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Heavy);
    holdTimer.current = setTimeout(async () => {
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Error);
      const loc = await LocationService.getCurrentLocation();
      await EmergencyService.triggerSOS(state.user.id, loc || { latitude: 0, longitude: 0 }, { trailId: state.currentHike.trailId });
      dispatch({ type: 'TRIGGER_SOS' });
      setIsHolding(false);
    }, 3000);
    Animated.timing(holdProgress, { toValue: 1, duration: 3000, useNativeDriver: false }).start();
  };

  const cancelHold = () => {
    setIsHolding(false);
    if (holdTimer.current) clearTimeout(holdTimer.current);
    Animated.timing(holdProgress, { toValue: 0, duration: 200, useNativeDriver: false }).start();
  };

  const cancelSOS = () => {
    Alert.alert('Cancel SOS', 'Are you sure you want to cancel the emergency alert?', [
      { text: 'Keep Active', style: 'cancel' },
      {
        text: 'Cancel SOS', style: 'destructive',
        onPress: async () => {
          await EmergencyService.cancelSOS(state.user.id);
          dispatch({ type: 'CANCEL_SOS' });
        },
      },
    ]);
  };

  const triggerSilentSOS = async () => {
    const loc = await LocationService.getCurrentLocation();
    await EmergencyService.triggerSilentSOS(state.user.id, loc || { latitude: 0, longitude: 0 });
    dispatch({ type: 'TRIGGER_SOS' });
    Alert.alert('Silent SOS', 'Silent emergency alert has been sent to your contacts and authorities.');
  };

  const progressWidth = holdProgress.interpolate({ inputRange: [0, 1], outputRange: ['0%', '100%'] });

  if (isSOSActive) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: '#B71C1C' }]}>
        <Animated.View style={[styles.activeContainer, { transform: [{ scale: pulseAnim }] }]}>
          <Ionicons name="alert-circle" size={80} color="#fff" />
          <Text style={styles.activeTitle}>SOS ACTIVE</Text>
          <Text style={styles.activeTime}>Sent: {state.safetyStatus.sosTimestamp ? new Date(state.safetyStatus.sosTimestamp).toLocaleTimeString() : 'Just now'}</Text>
        </Animated.View>
        <View style={styles.activeInfo}>
          <View style={styles.infoRow}>
            <Ionicons name="location" size={20} color="#fff" />
            <Text style={styles.infoText}>Location is being shared continuously</Text>
          </View>
          <View style={styles.infoRow}>
            <Ionicons name="people" size={20} color="#fff" />
            <Text style={styles.infoText}>Forest officers and contacts notified</Text>
          </View>
          <View style={styles.infoRow}>
            <Ionicons name={state.networkStatus.connected ? 'cloud-done' : 'cloud-offline'} size={20} color="#fff" />
            <Text style={styles.infoText}>{state.networkStatus.connected ? 'Alert sent successfully' : 'Alert queued - will send when connected'}</Text>
          </View>
        </View>
        {/* Emergency contacts quick call */}
        <View style={styles.contactsSection}>
          {state.user.emergencyContacts.map((c, i) => (
            <TouchableOpacity key={i} style={styles.callBtn} onPress={() => Linking.openURL('tel:' + c.phone)}>
              <Ionicons name="call" size={20} color={colors.danger} />
              <Text style={styles.callText}>{c.name}</Text>
            </TouchableOpacity>
          ))}
        </View>
        <TouchableOpacity style={styles.cancelSOSBtn} onPress={cancelSOS}>
          <Text style={styles.cancelSOSText}>Cancel SOS</Text>
        </TouchableOpacity>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.mainContent}>
        <Text style={styles.title}>Emergency SOS</Text>
        <Text style={styles.subtitle}>Press and hold the button for 3 seconds to trigger an emergency alert</Text>

        {/* SOS Button */}
        <View style={styles.sosButtonContainer}>
          <TouchableOpacity
            style={styles.sosButton}
            onPressIn={startHold}
            onPressOut={cancelHold}
            activeOpacity={0.9}
          >
            <View style={styles.sosInner}>
              <Ionicons name="alert-circle" size={50} color="#fff" />
              <Text style={styles.sosText}>SOS</Text>
              <Text style={styles.sosHint}>{isHolding ? 'Keep holding...' : 'Press & Hold'}</Text>
            </View>
            {/* Progress overlay */}
            <Animated.View style={[styles.sosProgress, { width: progressWidth }]} />
          </TouchableOpacity>
        </View>

        {/* Silent SOS */}
        <TouchableOpacity style={styles.silentBtn} onPress={triggerSilentSOS}>
          <Ionicons name="finger-print" size={20} color={colors.textLight} />
          <Text style={styles.silentText}>Silent SOS (no sound or visual alert)</Text>
        </TouchableOpacity>
      </View>

      {/* Info */}
      <View style={styles.infoSection}>
        <Text style={styles.infoTitle}>What happens when you trigger SOS:</Text>
        <Text style={styles.infoItem}>1. Your GPS location is shared immediately</Text>
        <Text style={styles.infoItem}>2. Forest officers are notified</Text>
        <Text style={styles.infoItem}>3. Emergency contacts receive an alert</Text>
        <Text style={styles.infoItem}>4. Continuous location updates begin</Text>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  mainContent: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: spacing.lg },
  title: { fontSize: 28, fontWeight: '800', color: colors.text, marginBottom: spacing.sm },
  subtitle: { fontSize: 15, color: colors.textLight, textAlign: 'center', lineHeight: 22, marginBottom: spacing.xl },
  sosButtonContainer: { marginVertical: spacing.xl },
  sosButton: { width: 180, height: 180, borderRadius: 90, backgroundColor: colors.danger, justifyContent: 'center', alignItems: 'center', ...shadows.large, overflow: 'hidden' },
  sosInner: { alignItems: 'center', zIndex: 2 },
  sosText: { fontSize: 32, fontWeight: '900', color: '#fff', letterSpacing: 4, marginTop: 4 },
  sosHint: { fontSize: 12, color: 'rgba(255,255,255,0.7)', marginTop: 4 },
  sosProgress: { position: 'absolute', left: 0, top: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.3)', zIndex: 1 },
  silentBtn: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, paddingVertical: spacing.md },
  silentText: { fontSize: 14, color: colors.textLight },
  infoSection: { backgroundColor: colors.surface, padding: spacing.lg, borderTopWidth: 1, borderTopColor: colors.border },
  infoTitle: { fontSize: 14, fontWeight: '600', color: colors.text, marginBottom: spacing.sm },
  infoItem: { fontSize: 13, color: colors.textLight, lineHeight: 22 },
  // Active SOS styles
  activeContainer: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  activeTitle: { fontSize: 36, fontWeight: '900', color: '#fff', letterSpacing: 3, marginTop: spacing.md },
  activeTime: { fontSize: 14, color: 'rgba(255,255,255,0.8)', marginTop: spacing.sm },
  activeInfo: { padding: spacing.lg, gap: spacing.sm },
  infoRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  infoText: { color: '#fff', fontSize: 14 },
  contactsSection: { padding: spacing.md, gap: spacing.sm },
  callBtn: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#fff', padding: spacing.md, borderRadius: borderRadius.lg, gap: spacing.sm },
  callText: { fontSize: 15, fontWeight: '600', color: colors.danger },
  cancelSOSBtn: { margin: spacing.lg, padding: spacing.md, borderRadius: borderRadius.lg, borderWidth: 2, borderColor: '#fff', alignItems: 'center' },
  cancelSOSText: { color: '#fff', fontSize: 16, fontWeight: '700' },
});

export default SOSScreen;
