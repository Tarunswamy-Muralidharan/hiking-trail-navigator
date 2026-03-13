import React from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { useAppContext } from '../store/AppContext';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';
import { formatDuration } from '../constants/trailData';

const ActivityHistoryScreen = () => {
  const navigation = useNavigation();
  const { state } = useAppContext();
  const activities = state.activities || [];

  const totalStats = activities.reduce(
    (acc, a) => ({
      hikes: acc.hikes + 1,
      distance: acc.distance + (a.distance || 0),
      duration: acc.duration + (a.duration || 0),
    }),
    { hikes: 0, distance: 0, duration: 0 }
  );

  const renderActivity = ({ item }) => (
    <TouchableOpacity style={styles.card} onPress={() => navigation.navigate('Activity', { activity: item })} activeOpacity={0.7}>
      <View style={styles.cardHeader}>
        <View>
          <Text style={styles.cardTitle}>{item.trailName}</Text>
          <Text style={styles.cardDate}>{new Date(item.startTime).toLocaleDateString()}</Text>
        </View>
        <Ionicons name="chevron-forward" size={20} color={colors.textLight} />
      </View>
      <View style={styles.cardStats}>
        <View style={styles.cardStat}>
          <Ionicons name="map-outline" size={14} color={colors.textLight} />
          <Text style={styles.cardStatText}>{(item.distance || 0).toFixed(1)} km</Text>
        </View>
        <View style={styles.cardStat}>
          <Ionicons name="time-outline" size={14} color={colors.textLight} />
          <Text style={styles.cardStatText}>{formatDuration(Math.round(item.duration || 0))}</Text>
        </View>
        <View style={styles.cardStat}>
          <Ionicons name="trending-up" size={14} color={colors.textLight} />
          <Text style={styles.cardStatText}>{Math.round(item.elevationGain || 0)}m</Text>
        </View>
      </View>
    </TouchableOpacity>
  );

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      {/* Summary */}
      <View style={styles.summary}>
        <View style={styles.summaryItem}>
          <Text style={styles.summaryValue}>{totalStats.hikes}</Text>
          <Text style={styles.summaryLabel}>Hikes</Text>
        </View>
        <View style={styles.summaryItem}>
          <Text style={styles.summaryValue}>{totalStats.distance.toFixed(1)}</Text>
          <Text style={styles.summaryLabel}>Total km</Text>
        </View>
        <View style={styles.summaryItem}>
          <Text style={styles.summaryValue}>{formatDuration(Math.round(totalStats.duration))}</Text>
          <Text style={styles.summaryLabel}>Total Time</Text>
        </View>
      </View>

      <FlatList
        data={activities}
        keyExtractor={(item) => item.id}
        renderItem={renderActivity}
        contentContainerStyle={styles.list}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Ionicons name="footsteps-outline" size={60} color={colors.border} />
            <Text style={styles.emptyText}>No hikes yet</Text>
            <Text style={styles.emptySubtext}>Start your first hike to see it here</Text>
          </View>
        }
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  summary: { flexDirection: 'row', justifyContent: 'space-around', backgroundColor: colors.primary, padding: spacing.lg },
  summaryItem: { alignItems: 'center' },
  summaryValue: { fontSize: 22, fontWeight: '700', color: '#fff' },
  summaryLabel: { fontSize: 12, color: 'rgba(255,255,255,0.8)', marginTop: 2 },
  list: { padding: spacing.md },
  card: { backgroundColor: colors.surface, borderRadius: borderRadius.lg, padding: spacing.md, marginBottom: spacing.sm, ...shadows.small },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: spacing.sm },
  cardTitle: { fontSize: 16, fontWeight: '600', color: colors.text },
  cardDate: { fontSize: 12, color: colors.textLight, marginTop: 2 },
  cardStats: { flexDirection: 'row', gap: spacing.lg },
  cardStat: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  cardStatText: { fontSize: 13, color: colors.textLight },
  empty: { alignItems: 'center', paddingTop: 80, gap: spacing.sm },
  emptyText: { fontSize: 18, fontWeight: '600', color: colors.textLight },
  emptySubtext: { fontSize: 14, color: colors.textLight },
});

export default ActivityHistoryScreen;
