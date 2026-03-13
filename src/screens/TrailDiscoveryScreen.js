import React, { useState, useMemo, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, TextInput, StyleSheet, RefreshControl } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation, useRoute } from '@react-navigation/native';
import { colors, spacing, borderRadius } from '../constants/theme';
import { trails } from '../constants/trailData';
import { searchTrails, sortTrails } from '../services/TrailService';
import TrailCard from '../components/TrailCard';

const DIFFICULTIES = ['All', 'Easy', 'Moderate', 'Hard', 'Expert'];
const SORT_OPTIONS = ['popularity', 'distance', 'rating', 'difficulty'];

const TrailDiscoveryScreen = () => {
  const navigation = useNavigation();
  const route = useRoute();
  const [query, setQuery] = useState(route.params?.query || '');
  const [selectedDifficulty, setSelectedDifficulty] = useState('All');
  const [sortBy, setSortBy] = useState('popularity');
  const [refreshing, setRefreshing] = useState(false);

  const filteredTrails = useMemo(() => {
    const filters = {};
    if (query) filters.query = query;
    if (selectedDifficulty !== 'All') filters.difficulty = selectedDifficulty;
    let results = searchTrails(filters);
    return sortTrails(results, sortBy);
  }, [query, selectedDifficulty, sortBy]);

  const handleTrailPress = useCallback((trail) => {
    navigation.navigate('TrailDetail', { trail });
  }, [navigation]);

  const onRefresh = () => {
    setRefreshing(true);
    setTimeout(() => setRefreshing(false), 1000);
  };

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      {/* Search */}
      <View style={styles.searchBar}>
        <Ionicons name="search" size={20} color={colors.textLight} />
        <TextInput
          style={styles.searchInput}
          placeholder="Search by name, region..."
          placeholderTextColor={colors.textLight}
          value={query}
          onChangeText={setQuery}
        />
        {query.length > 0 && (
          <TouchableOpacity onPress={() => setQuery('')}>
            <Ionicons name="close-circle" size={20} color={colors.textLight} />
          </TouchableOpacity>
        )}
      </View>

      {/* Difficulty filter chips */}
      <View style={styles.filterRow}>
        {DIFFICULTIES.map((d) => (
          <TouchableOpacity
            key={d}
            style={[styles.chip, selectedDifficulty === d && styles.chipActive]}
            onPress={() => setSelectedDifficulty(d)}
          >
            <Text style={[styles.chipText, selectedDifficulty === d && styles.chipTextActive]}>{d}</Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Sort */}
      <View style={styles.sortRow}>
        <Text style={styles.resultCount}>{filteredTrails.length} trails found</Text>
        <View style={styles.sortBtns}>
          {SORT_OPTIONS.map((opt) => (
            <TouchableOpacity key={opt} onPress={() => setSortBy(opt)} style={[styles.sortBtn, sortBy === opt && styles.sortBtnActive]}>
              <Text style={[styles.sortText, sortBy === opt && styles.sortTextActive]}>
                {opt.charAt(0).toUpperCase() + opt.slice(1)}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>

      {/* Trail list */}
      <FlatList
        data={filteredTrails}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => <TrailCard trail={item} onPress={handleTrailPress} />}
        contentContainerStyle={styles.list}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.primary} />}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Ionicons name="trail-sign-outline" size={60} color={colors.border} />
            <Text style={styles.emptyText}>No trails found</Text>
            <Text style={styles.emptySubtext}>Try adjusting your filters</Text>
          </View>
        }
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  searchBar: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, margin: spacing.md, borderRadius: borderRadius.lg, paddingHorizontal: spacing.md, height: 44, gap: spacing.sm },
  searchInput: { flex: 1, fontSize: 15, color: colors.text },
  filterRow: { flexDirection: 'row', paddingHorizontal: spacing.md, gap: spacing.sm, marginBottom: spacing.sm },
  chip: { paddingHorizontal: 14, paddingVertical: 6, borderRadius: borderRadius.full, backgroundColor: colors.surface, borderWidth: 1, borderColor: colors.border },
  chipActive: { backgroundColor: colors.primary, borderColor: colors.primary },
  chipText: { fontSize: 13, color: colors.text, fontWeight: '500' },
  chipTextActive: { color: '#fff' },
  sortRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: spacing.md, marginBottom: spacing.sm },
  resultCount: { fontSize: 13, color: colors.textLight },
  sortBtns: { flexDirection: 'row', gap: 4 },
  sortBtn: { paddingHorizontal: 8, paddingVertical: 4, borderRadius: borderRadius.sm },
  sortBtnActive: { backgroundColor: colors.primaryLight + '20' },
  sortText: { fontSize: 11, color: colors.textLight },
  sortTextActive: { color: colors.primary, fontWeight: '600' },
  list: { paddingBottom: spacing.xl },
  empty: { alignItems: 'center', paddingTop: 80, gap: spacing.sm },
  emptyText: { fontSize: 18, fontWeight: '600', color: colors.textLight },
  emptySubtext: { fontSize: 14, color: colors.textLight },
});

export default TrailDiscoveryScreen;
