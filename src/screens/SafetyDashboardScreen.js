import React from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, Switch } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { useAppContext } from '../store/AppContext';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';

const SafetyDashboardScreen = () => {
  const navigation = useNavigation();
  const { state, dispatch } = useAppContext();
  const { safetyStatus, currentHike, user, networkStatus, riskLevel } = state;

  const statusColor = safetyStatus.sosActive ? colors.danger
    : safetyStatus.fallDetected ? colors.warning
    : colors.primary;
  const statusText = safetyStatus.sosActive ? 'EMERGENCY ACTIVE'
    : safetyStatus.fallDetected ? 'FALL DETECTED'
    : currentHike.active ? 'HIKING - SAFE'
    : 'ALL CLEAR';

  const toggleSetting = (key) => {
    dispatch({ type: 'UPDATE_SETTINGS', payload: { [key]: !user.preferences[key] } });
  };

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Status card */}
      <View style={[styles.statusCard, { backgroundColor: statusColor }]}>
        <Ionicons name={safetyStatus.sosActive ? 'alert-circle' : 'shield-checkmark'} size={40} color="#fff" />
        <Text style={styles.statusText}>{statusText}</Text>
        {currentHike.active && safetyStatus.lastCheckIn && (
          <Text style={styles.statusSub}>Last check-in: {new Date(safetyStatus.lastCheckIn).toLocaleTimeString()}</Text>
        )}
        {!networkStatus.connected && (
          <View style={styles.offlineTag}>
            <Ionicons name="cloud-offline" size={14} color="#fff" />
            <Text style={styles.offlineTagText}>Offline</Text>
          </View>
        )}
      </View>

      {/* Quick actions */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Quick Actions</Text>
        <View style={styles.actionsGrid}>
          <TouchableOpacity style={[styles.actionBtn, { backgroundColor: colors.danger }]} onPress={() => navigation.navigate('SOS')}>
            <Ionicons name="alert-circle" size={28} color="#fff" />
            <Text style={styles.actionText}>SOS</Text>
          </TouchableOpacity>
          <TouchableOpacity style={[styles.actionBtn, { backgroundColor: colors.info }]} onPress={() => navigation.navigate('LiveTracking')}>
            <Ionicons name="location" size={28} color="#fff" />
            <Text style={styles.actionText}>Share Location</Text>
          </TouchableOpacity>
          <TouchableOpacity style={[styles.actionBtn, { backgroundColor: colors.secondary }]} onPress={() => navigation.navigate('HazardReport')}>
            <Ionicons name="warning" size={28} color="#fff" />
            <Text style={styles.actionText}>Report Hazard</Text>
          </TouchableOpacity>
          <TouchableOpacity style={[styles.actionBtn, { backgroundColor: '#9C27B0' }]} onPress={() => navigation.navigate('EmergencyContacts')}>
            <Ionicons name="call" size={28} color="#fff" />
            <Text style={styles.actionText}>Contacts</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Safety features */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Safety Features</Text>
        <View style={styles.featureCard}>
          <View style={styles.featureRow}>
            <View style={styles.featureInfo}>
              <Ionicons name="timer-outline" size={22} color={colors.primary} />
              <View>
                <Text style={styles.featureName}>Periodic Check-ins</Text>
                <Text style={styles.featureSub}>Every {user.preferences.checkInInterval} min</Text>
              </View>
            </View>
            <Switch
              value={true}
              trackColor={{ true: colors.primary }}
              thumbColor="#fff"
            />
          </View>
          <View style={styles.featureRow}>
            <View style={styles.featureInfo}>
              <Ionicons name="body-outline" size={22} color={colors.primary} />
              <View>
                <Text style={styles.featureName}>Fall Detection</Text>
                <Text style={styles.featureSub}>Auto-detect using sensors</Text>
              </View>
            </View>
            <Switch
              value={user.preferences.fallDetectionEnabled}
              onValueChange={() => toggleSetting('fallDetectionEnabled')}
              trackColor={{ true: colors.primary }}
              thumbColor="#fff"
            />
          </View>
          <View style={styles.featureRow}>
            <View style={styles.featureInfo}>
              <Ionicons name="navigate-outline" size={22} color={colors.primary} />
              <View>
                <Text style={styles.featureName}>Live Location Sharing</Text>
                <Text style={styles.featureSub}>Share with contacts</Text>
              </View>
            </View>
            <Switch
              value={user.preferences.locationShareEnabled}
              onValueChange={() => toggleSetting('locationShareEnabled')}
              trackColor={{ true: colors.primary }}
              thumbColor="#fff"
            />
          </View>
          <View style={styles.featureRow}>
            <View style={styles.featureInfo}>
              <Ionicons name="finger-print-outline" size={22} color={colors.primary} />
              <View>
                <Text style={styles.featureName}>Silent SOS</Text>
                <Text style={styles.featureSub}>Discreet emergency alert</Text>
              </View>
            </View>
            <Switch
              value={user.preferences.silentSOSEnabled}
              onValueChange={() => toggleSetting('silentSOSEnabled')}
              trackColor={{ true: colors.primary }}
              thumbColor="#fff"
            />
          </View>
        </View>
      </View>

      {/* Risk level */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Current Risk Level</Text>
        <View style={styles.riskCard}>
          <Ionicons
            name="shield"
            size={32}
            color={riskLevel === 'low' ? colors.primary : riskLevel === 'medium' ? colors.warning : colors.danger}
          />
          <View>
            <Text style={styles.riskLevel}>{riskLevel.toUpperCase()}</Text>
            <Text style={styles.riskSub}>
              {riskLevel === 'low' ? 'Conditions are favorable' : riskLevel === 'medium' ? 'Exercise normal caution' : 'Enhanced monitoring active'}
            </Text>
          </View>
        </View>
      </View>

      {/* Network */}
      <View style={[styles.section, { marginBottom: spacing.xxl }]}>
        <Text style={styles.sectionTitle}>Network Status</Text>
        <View style={styles.networkCard}>
          <Ionicons
            name={networkStatus.connected ? 'cellular' : 'cellular-outline'}
            size={24}
            color={networkStatus.connected ? colors.primary : colors.danger}
          />
          <View>
            <Text style={styles.networkText}>{networkStatus.connected ? 'Connected' : 'Offline'}</Text>
            <Text style={styles.networkSub}>Signal: {networkStatus.signalStrength}</Text>
          </View>
        </View>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  statusCard: { padding: spacing.xl, alignItems: 'center', gap: spacing.sm },
  statusText: { fontSize: 20, fontWeight: '800', color: '#fff', letterSpacing: 1 },
  statusSub: { fontSize: 13, color: 'rgba(255,255,255,0.8)' },
  offlineTag: { flexDirection: 'row', alignItems: 'center', gap: 4, backgroundColor: 'rgba(0,0,0,0.2)', paddingHorizontal: 10, paddingVertical: 4, borderRadius: borderRadius.full, marginTop: 4 },
  offlineTagText: { color: '#fff', fontSize: 12, fontWeight: '600' },
  section: { padding: spacing.md },
  sectionTitle: { fontSize: 18, fontWeight: '700', color: colors.text, marginBottom: spacing.sm },
  actionsGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  actionBtn: { flex: 1, minWidth: '45%', alignItems: 'center', padding: spacing.md, borderRadius: borderRadius.lg, gap: 6, ...shadows.small },
  actionText: { color: '#fff', fontWeight: '700', fontSize: 12 },
  featureCard: { backgroundColor: colors.surface, borderRadius: borderRadius.lg, overflow: 'hidden', ...shadows.small },
  featureRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: spacing.md, borderBottomWidth: 1, borderBottomColor: colors.border },
  featureInfo: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  featureName: { fontSize: 15, fontWeight: '600', color: colors.text },
  featureSub: { fontSize: 12, color: colors.textLight },
  riskCard: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, backgroundColor: colors.surface, padding: spacing.md, borderRadius: borderRadius.lg, ...shadows.small },
  riskLevel: { fontSize: 18, fontWeight: '700', color: colors.text },
  riskSub: { fontSize: 13, color: colors.textLight },
  networkCard: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, backgroundColor: colors.surface, padding: spacing.md, borderRadius: borderRadius.lg, ...shadows.small },
  networkText: { fontSize: 15, fontWeight: '600', color: colors.text },
  networkSub: { fontSize: 12, color: colors.textLight },
});

export default SafetyDashboardScreen;
