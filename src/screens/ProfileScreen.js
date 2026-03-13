import React from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { useAppContext } from '../store/AppContext';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';
import { formatDuration } from '../constants/trailData';

const ProfileScreen = () => {
  const navigation = useNavigation();
  const { state } = useAppContext();
  const { user, activities } = state;

  const totalStats = activities.reduce(
    (acc, a) => ({ hikes: acc.hikes + 1, distance: acc.distance + (a.distance || 0), duration: acc.duration + (a.duration || 0) }),
    { hikes: 0, distance: 0, duration: 0 }
  );

  const initials = user.name.split(' ').map((n) => n[0]).join('').toUpperCase();

  const badges = [
    { icon: 'flag', label: 'First Hike', color: colors.primary, earned: totalStats.hikes >= 1 },
    { icon: 'map', label: '50km Club', color: '#9C27B0', earned: totalStats.distance >= 50 },
    { icon: 'shield-checkmark', label: 'Safety Pro', color: colors.info, earned: true },
    { icon: 'megaphone', label: 'Trail Reporter', color: colors.secondary, earned: state.hazards.length > 0 },
  ];

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Profile header */}
      <View style={styles.header}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{initials}</Text>
        </View>
        <Text style={styles.name}>{user.name}</Text>
        <Text style={styles.email}>{user.email}</Text>
      </View>

      {/* Stats */}
      <View style={styles.statsRow}>
        <View style={styles.statBox}>
          <Text style={styles.statValue}>{totalStats.hikes}</Text>
          <Text style={styles.statLabel}>Hikes</Text>
        </View>
        <View style={styles.statBox}>
          <Text style={styles.statValue}>{totalStats.distance.toFixed(1)}</Text>
          <Text style={styles.statLabel}>km Total</Text>
        </View>
        <View style={styles.statBox}>
          <Text style={styles.statValue}>{formatDuration(Math.round(totalStats.duration))}</Text>
          <Text style={styles.statLabel}>Total Time</Text>
        </View>
      </View>

      {/* Badges */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Achievements</Text>
        <View style={styles.badgeRow}>
          {badges.map((b, i) => (
            <View key={i} style={[styles.badge, !b.earned && styles.badgeLocked]}>
              <Ionicons name={b.icon} size={24} color={b.earned ? b.color : colors.border} />
              <Text style={[styles.badgeLabel, !b.earned && styles.badgeLabelLocked]}>{b.label}</Text>
            </View>
          ))}
        </View>
      </View>

      {/* Menu items */}
      <View style={styles.section}>
        {[
          { icon: 'call-outline', label: 'Emergency Contacts', screen: 'EmergencyContacts', stack: 'Safety' },
          { icon: 'settings-outline', label: 'Settings', screen: 'Settings' },
        ].map((item, i) => (
          <TouchableOpacity
            key={i}
            style={styles.menuItem}
            onPress={() => {
              if (item.stack) navigation.navigate(item.stack, { screen: item.screen });
              else navigation.navigate(item.screen);
            }}
          >
            <Ionicons name={item.icon} size={22} color={colors.primary} />
            <Text style={styles.menuLabel}>{item.label}</Text>
            <Ionicons name="chevron-forward" size={20} color={colors.textLight} />
          </TouchableOpacity>
        ))}
      </View>

      {/* App info */}
      <View style={[styles.section, { marginBottom: spacing.xxl }]}>
        <Text style={styles.appInfo}>Hiking Trail Navigator v1.0.0</Text>
        <Text style={styles.appInfo}>OOSE Lab - Academic Project</Text>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  header: { alignItems: 'center', paddingVertical: spacing.xl, backgroundColor: colors.primary },
  avatar: { width: 80, height: 80, borderRadius: 40, backgroundColor: 'rgba(255,255,255,0.2)', justifyContent: 'center', alignItems: 'center' },
  avatarText: { fontSize: 30, fontWeight: '700', color: '#fff' },
  name: { fontSize: 22, fontWeight: '700', color: '#fff', marginTop: spacing.sm },
  email: { fontSize: 14, color: 'rgba(255,255,255,0.8)', marginTop: 2 },
  statsRow: { flexDirection: 'row', justifyContent: 'space-around', backgroundColor: colors.surface, marginHorizontal: spacing.md, marginTop: -20, borderRadius: borderRadius.lg, padding: spacing.md, ...shadows.medium },
  statBox: { alignItems: 'center' },
  statValue: { fontSize: 22, fontWeight: '700', color: colors.text },
  statLabel: { fontSize: 12, color: colors.textLight, marginTop: 2 },
  section: { padding: spacing.md },
  sectionTitle: { fontSize: 18, fontWeight: '700', color: colors.text, marginBottom: spacing.sm },
  badgeRow: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  badge: { width: '23%', alignItems: 'center', padding: spacing.sm, borderRadius: borderRadius.lg, backgroundColor: colors.surface, ...shadows.small },
  badgeLocked: { opacity: 0.4 },
  badgeLabel: { fontSize: 10, color: colors.text, fontWeight: '600', textAlign: 'center', marginTop: 4 },
  badgeLabelLocked: { color: colors.textLight },
  menuItem: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: spacing.md, borderRadius: borderRadius.lg, marginBottom: spacing.sm, ...shadows.small, gap: spacing.sm },
  menuLabel: { flex: 1, fontSize: 15, fontWeight: '500', color: colors.text },
  appInfo: { fontSize: 12, color: colors.textLight, textAlign: 'center' },
});

export default ProfileScreen;
