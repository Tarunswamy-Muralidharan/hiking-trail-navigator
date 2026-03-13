import React, { useState } from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, Switch, Alert } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useAppContext } from '../store/AppContext';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';
import { clearCache, getCacheSize } from '../services/OfflineStorageService';

const SettingsScreen = () => {
  const { state, dispatch } = useAppContext();
  const prefs = state.user.preferences;
  const [cacheSize, setCacheSize] = useState('Calculating...');

  React.useEffect(() => {
    getCacheSize().then((s) => setCacheSize(s.formatted));
  }, []);

  const updatePref = (key, value) => {
    dispatch({ type: 'UPDATE_SETTINGS', payload: { [key]: value } });
  };

  const SettingRow = ({ icon, label, value, children }) => (
    <View style={styles.settingRow}>
      <View style={styles.settingInfo}>
        <Ionicons name={icon} size={20} color={colors.primary} />
        <View>
          <Text style={styles.settingLabel}>{label}</Text>
          {value && <Text style={styles.settingValue}>{value}</Text>}
        </View>
      </View>
      {children}
    </View>
  );

  const SectionHeader = ({ title }) => <Text style={styles.sectionTitle}>{title}</Text>;

  const IntervalSelector = ({ values, current, onChange }) => (
    <View style={styles.selectorRow}>
      {values.map((v) => (
        <TouchableOpacity key={v.value} style={[styles.selectorBtn, current === v.value && styles.selectorBtnActive]} onPress={() => onChange(v.value)}>
          <Text style={[styles.selectorText, current === v.value && styles.selectorTextActive]}>{v.label}</Text>
        </TouchableOpacity>
      ))}
    </View>
  );

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Safety */}
      <SectionHeader title="Safety Settings" />
      <View style={styles.card}>
        <Text style={styles.subLabel}>Check-in Interval</Text>
        <IntervalSelector
          values={[{ label: '15m', value: 15 }, { label: '30m', value: 30 }, { label: '1h', value: 60 }, { label: '2h', value: 120 }]}
          current={prefs.checkInInterval}
          onChange={(v) => updatePref('checkInInterval', v)}
        />
        <SettingRow icon="body-outline" label="Fall Detection">
          <Switch value={prefs.fallDetectionEnabled} onValueChange={(v) => updatePref('fallDetectionEnabled', v)} trackColor={{ true: colors.primary }} />
        </SettingRow>
        <SettingRow icon="finger-print" label="Silent SOS Gesture">
          <Switch value={prefs.silentSOSEnabled} onValueChange={(v) => updatePref('silentSOSEnabled', v)} trackColor={{ true: colors.primary }} />
        </SettingRow>
      </View>

      {/* Navigation */}
      <SectionHeader title="Navigation Settings" />
      <View style={styles.card}>
        <Text style={styles.subLabel}>Route Deviation Alert Distance</Text>
        <IntervalSelector
          values={[{ label: '50m', value: 50 }, { label: '100m', value: 100 }, { label: '200m', value: 200 }]}
          current={prefs.deviationAlertDistance}
          onChange={(v) => updatePref('deviationAlertDistance', v)}
        />
        <Text style={styles.subLabel}>GPS Accuracy Mode</Text>
        <IntervalSelector
          values={[{ label: 'High', value: 'high' }, { label: 'Balanced', value: 'balanced' }, { label: 'Low Power', value: 'low' }]}
          current={prefs.gpsAccuracy}
          onChange={(v) => updatePref('gpsAccuracy', v)}
        />
      </View>

      {/* Privacy */}
      <SectionHeader title="Privacy Settings" />
      <View style={styles.card}>
        <SettingRow icon="location-outline" label="Location Sharing Consent">
          <Switch value={prefs.locationShareEnabled} onValueChange={(v) => updatePref('locationShareEnabled', v)} trackColor={{ true: colors.primary }} />
        </SettingRow>
      </View>

      {/* Data */}
      <SectionHeader title="Data Management" />
      <View style={styles.card}>
        <SettingRow icon="folder-outline" label="Cache Size" value={cacheSize} />
        <TouchableOpacity style={styles.actionRow} onPress={() => {
          Alert.alert('Clear Cache', 'Remove all cached data?', [
            { text: 'Cancel', style: 'cancel' },
            { text: 'Clear', style: 'destructive', onPress: async () => { await clearCache(); setCacheSize('0 KB'); } },
          ]);
        }}>
          <Ionicons name="trash-outline" size={20} color={colors.danger} />
          <Text style={[styles.settingLabel, { color: colors.danger }]}>Clear Cache</Text>
        </TouchableOpacity>
      </View>

      {/* About */}
      <SectionHeader title="About" />
      <View style={[styles.card, { marginBottom: spacing.xxl }]}>
        <SettingRow icon="information-circle-outline" label="Version" value="1.0.0" />
        <SettingRow icon="school-outline" label="Project" value="OOSE Lab - Academic" />
        <SettingRow icon="person-outline" label="Developer" value="Poornesh P" />
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  sectionTitle: { fontSize: 16, fontWeight: '700', color: colors.primary, paddingHorizontal: spacing.md, paddingTop: spacing.md, paddingBottom: spacing.xs },
  card: { backgroundColor: colors.surface, marginHorizontal: spacing.md, borderRadius: borderRadius.lg, overflow: 'hidden', ...shadows.small, marginBottom: spacing.sm },
  settingRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: spacing.md, borderBottomWidth: 1, borderBottomColor: colors.border },
  settingInfo: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, flex: 1 },
  settingLabel: { fontSize: 15, fontWeight: '500', color: colors.text },
  settingValue: { fontSize: 12, color: colors.textLight },
  subLabel: { fontSize: 13, fontWeight: '600', color: colors.textLight, paddingHorizontal: spacing.md, paddingTop: spacing.sm },
  selectorRow: { flexDirection: 'row', gap: spacing.xs, padding: spacing.sm, paddingHorizontal: spacing.md },
  selectorBtn: { flex: 1, padding: spacing.sm, borderRadius: borderRadius.md, backgroundColor: colors.background, alignItems: 'center' },
  selectorBtnActive: { backgroundColor: colors.primary },
  selectorText: { fontSize: 13, fontWeight: '600', color: colors.text },
  selectorTextActive: { color: '#fff' },
  actionRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, padding: spacing.md, borderBottomWidth: 1, borderBottomColor: colors.border },
});

export default SettingsScreen;
