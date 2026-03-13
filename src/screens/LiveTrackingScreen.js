import React, { useState } from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, Switch, Alert } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useAppContext } from '../store/AppContext';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';

const INTERVALS = [
  { label: '30 sec', value: 30 },
  { label: '1 min', value: 60 },
  { label: '5 min', value: 300 },
];

const LiveTrackingScreen = () => {
  const { state, dispatch } = useAppContext();
  const [isSharing, setIsSharing] = useState(state.safetyStatus.isSharing);
  const [interval, setInterval_] = useState(60);

  const toggleSharing = () => {
    setIsSharing(!isSharing);
    Alert.alert(
      isSharing ? 'Stop Sharing' : 'Start Sharing',
      isSharing ? 'Location sharing has been stopped.' : 'Your location is now being shared with your contacts.'
    );
  };

  const contacts = state.user.emergencyContacts;

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Sharing status */}
      <View style={[styles.statusCard, { backgroundColor: isSharing ? colors.primary : colors.textLight }]}>
        <Ionicons name={isSharing ? 'radio' : 'radio-outline'} size={40} color="#fff" />
        <Text style={styles.statusTitle}>{isSharing ? 'Sharing Active' : 'Sharing Inactive'}</Text>
        <Text style={styles.statusSub}>{isSharing ? 'Your location is being shared' : 'Enable to share your real-time location'}</Text>
      </View>

      {/* Toggle */}
      <View style={styles.section}>
        <View style={styles.toggleRow}>
          <Text style={styles.toggleLabel}>Live Location Sharing</Text>
          <Switch value={isSharing} onValueChange={toggleSharing} trackColor={{ true: colors.primary }} thumbColor="#fff" />
        </View>
      </View>

      {/* Interval */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Update Interval</Text>
        <View style={styles.intervalRow}>
          {INTERVALS.map((item) => (
            <TouchableOpacity
              key={item.value}
              style={[styles.intervalBtn, interval === item.value && styles.intervalBtnActive]}
              onPress={() => setInterval_(item.value)}
            >
              <Text style={[styles.intervalText, interval === item.value && styles.intervalTextActive]}>{item.label}</Text>
            </TouchableOpacity>
          ))}
        </View>
        <Text style={styles.batteryNote}>
          <Ionicons name="battery-half" size={14} /> Battery impact: {interval <= 30 ? 'High' : interval <= 60 ? 'Medium' : 'Low'}
        </Text>
      </View>

      {/* Recipients */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Sharing With</Text>
        {contacts.map((c, i) => (
          <View key={i} style={styles.contactRow}>
            <View style={styles.contactIcon}>
              <Ionicons name="person" size={20} color={colors.primary} />
            </View>
            <View style={styles.contactInfo}>
              <Text style={styles.contactName}>{c.name}</Text>
              <Text style={styles.contactRelation}>{c.relation}</Text>
            </View>
            <View style={[styles.contactStatus, { backgroundColor: isSharing ? '#E8F5E9' : '#F5F5F5' }]}>
              <Text style={[styles.contactStatusText, { color: isSharing ? colors.primary : colors.textLight }]}>
                {isSharing ? 'Receiving' : 'Inactive'}
              </Text>
            </View>
          </View>
        ))}
      </View>

      {/* Privacy notice */}
      <View style={[styles.section, { marginBottom: spacing.xxl }]}>
        <View style={styles.privacyCard}>
          <Ionicons name="lock-closed" size={20} color={colors.info} />
          <Text style={styles.privacyText}>
            Your location data is encrypted and shared only with selected contacts. Sharing stops automatically when your hike ends.
          </Text>
        </View>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  statusCard: { padding: spacing.xl, alignItems: 'center', gap: spacing.sm },
  statusTitle: { fontSize: 22, fontWeight: '700', color: '#fff' },
  statusSub: { fontSize: 14, color: 'rgba(255,255,255,0.8)' },
  section: { padding: spacing.md },
  sectionTitle: { fontSize: 18, fontWeight: '700', color: colors.text, marginBottom: spacing.sm },
  toggleRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', backgroundColor: colors.surface, padding: spacing.md, borderRadius: borderRadius.lg, ...shadows.small },
  toggleLabel: { fontSize: 16, fontWeight: '600', color: colors.text },
  intervalRow: { flexDirection: 'row', gap: spacing.sm },
  intervalBtn: { flex: 1, padding: spacing.sm, borderRadius: borderRadius.md, backgroundColor: colors.surface, alignItems: 'center', borderWidth: 1, borderColor: colors.border },
  intervalBtnActive: { backgroundColor: colors.primary, borderColor: colors.primary },
  intervalText: { fontSize: 14, fontWeight: '600', color: colors.text },
  intervalTextActive: { color: '#fff' },
  batteryNote: { fontSize: 12, color: colors.textLight, marginTop: spacing.sm },
  contactRow: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: spacing.md, borderRadius: borderRadius.md, marginBottom: spacing.sm, ...shadows.small },
  contactIcon: { width: 40, height: 40, borderRadius: 20, backgroundColor: '#E8F5E9', justifyContent: 'center', alignItems: 'center' },
  contactInfo: { flex: 1, marginLeft: spacing.sm },
  contactName: { fontSize: 15, fontWeight: '600', color: colors.text },
  contactRelation: { fontSize: 12, color: colors.textLight },
  contactStatus: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: borderRadius.full },
  contactStatusText: { fontSize: 12, fontWeight: '600' },
  privacyCard: { flexDirection: 'row', gap: spacing.sm, backgroundColor: '#E3F2FD', padding: spacing.md, borderRadius: borderRadius.lg },
  privacyText: { flex: 1, fontSize: 13, color: colors.info, lineHeight: 20 },
});

export default LiveTrackingScreen;
