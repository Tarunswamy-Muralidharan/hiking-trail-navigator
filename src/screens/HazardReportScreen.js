import React, { useState, useCallback } from 'react';
import { View, Text, ScrollView, TouchableOpacity, TextInput, StyleSheet, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { useAppContext } from '../store/AppContext';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';
import * as LocationService from '../services/LocationService';

const HAZARD_TYPES = [
  { id: 'wildlife', label: 'Wildlife', icon: 'paw' },
  { id: 'fallen_tree', label: 'Fallen Tree', icon: 'leaf' },
  { id: 'landslide', label: 'Landslide', icon: 'warning' },
  { id: 'terrain', label: 'Unsafe Terrain', icon: 'alert-circle' },
  { id: 'flood', label: 'Flood', icon: 'water' },
  { id: 'other', label: 'Other', icon: 'help-circle' },
];

const SEVERITIES = ['low', 'medium', 'high', 'critical'];

const HazardReportScreen = () => {
  const navigation = useNavigation();
  const { state, dispatch } = useAppContext();
  const [type, setType] = useState(null);
  const [severity, setSeverity] = useState(null);
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState(null);
  const [submitted, setSubmitted] = useState(false);

  React.useEffect(() => {
    (async () => {
      const loc = await LocationService.getCurrentLocation();
      if (loc) setLocation(loc);
    })();
  }, []);

  const handleSubmit = () => {
    if (!type || !severity || !description.trim()) {
      Alert.alert('Missing Info', 'Please fill in all required fields.');
      return;
    }
    const hazard = {
      id: 'hz-' + Date.now(),
      type,
      severity,
      description: description.trim(),
      latitude: location?.latitude || 0,
      longitude: location?.longitude || 0,
      reportedBy: state.user.name,
      timestamp: new Date().toISOString(),
      verified: false,
      confirmations: 0,
      trailId: state.currentHike.trailId,
    };
    dispatch({ type: 'ADD_HAZARD', payload: hazard });
    setSubmitted(true);
  };

  if (submitted) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.successContainer}>
          <Ionicons name="checkmark-circle" size={80} color={colors.primary} />
          <Text style={styles.successTitle}>Report Submitted!</Text>
          <Text style={styles.successSub}>Thank you for helping keep fellow hikers safe.</Text>
          <TouchableOpacity style={styles.doneBtn} onPress={() => navigation.goBack()}>
            <Text style={styles.doneBtnText}>Done</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }

  const sevColors = { low: '#4CAF50', medium: '#FF9800', high: '#F44336', critical: '#B71C1C' };

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      <View style={styles.content}>
        {/* Hazard type */}
        <Text style={styles.label}>Hazard Type *</Text>
        <View style={styles.typeGrid}>
          {HAZARD_TYPES.map((t) => (
            <TouchableOpacity
              key={t.id}
              style={[styles.typeBtn, type === t.id && styles.typeBtnActive]}
              onPress={() => setType(t.id)}
            >
              <Ionicons name={t.icon} size={24} color={type === t.id ? '#fff' : colors.text} />
              <Text style={[styles.typeText, type === t.id && styles.typeTextActive]}>{t.label}</Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Severity */}
        <Text style={styles.label}>Severity *</Text>
        <View style={styles.sevRow}>
          {SEVERITIES.map((s) => (
            <TouchableOpacity
              key={s}
              style={[styles.sevBtn, severity === s && { backgroundColor: sevColors[s] }]}
              onPress={() => setSeverity(s)}
            >
              <Text style={[styles.sevText, severity === s && { color: '#fff' }]}>
                {s.charAt(0).toUpperCase() + s.slice(1)}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Location */}
        <Text style={styles.label}>Location</Text>
        <View style={styles.locationCard}>
          <Ionicons name="location" size={20} color={colors.primary} />
          <Text style={styles.locationText}>
            {location ? location.latitude.toFixed(4) + ', ' + location.longitude.toFixed(4) : 'Getting location...'}
          </Text>
        </View>

        {/* Description */}
        <Text style={styles.label}>Description *</Text>
        <TextInput
          style={styles.descInput}
          placeholder="Describe the hazard in detail..."
          placeholderTextColor={colors.textLight}
          value={description}
          onChangeText={setDescription}
          multiline
          numberOfLines={4}
          textAlignVertical="top"
        />

        {/* Photo placeholder */}
        <TouchableOpacity style={styles.photoBtn}>
          <Ionicons name="camera-outline" size={24} color={colors.primary} />
          <Text style={styles.photoBtnText}>Add Photo (Coming Soon)</Text>
        </TouchableOpacity>

        {/* Submit */}
        <TouchableOpacity
          style={[styles.submitBtn, (!type || !severity || !description.trim()) && styles.submitBtnDisabled]}
          onPress={handleSubmit}
          disabled={!type || !severity || !description.trim()}
        >
          <Ionicons name="send" size={20} color="#fff" />
          <Text style={styles.submitText}>Submit Report</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: spacing.md },
  label: { fontSize: 16, fontWeight: '700', color: colors.text, marginBottom: spacing.sm, marginTop: spacing.md },
  typeGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  typeBtn: { width: '31%', alignItems: 'center', padding: spacing.md, borderRadius: borderRadius.lg, backgroundColor: colors.surface, borderWidth: 1, borderColor: colors.border, gap: 6 },
  typeBtnActive: { backgroundColor: colors.primary, borderColor: colors.primary },
  typeText: { fontSize: 12, fontWeight: '600', color: colors.text },
  typeTextActive: { color: '#fff' },
  sevRow: { flexDirection: 'row', gap: spacing.sm },
  sevBtn: { flex: 1, paddingVertical: 10, borderRadius: borderRadius.md, backgroundColor: colors.surface, alignItems: 'center', borderWidth: 1, borderColor: colors.border },
  sevText: { fontSize: 13, fontWeight: '600', color: colors.text },
  locationCard: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.surface, padding: spacing.md, borderRadius: borderRadius.lg },
  locationText: { fontSize: 14, color: colors.text },
  descInput: { backgroundColor: colors.surface, borderRadius: borderRadius.lg, padding: spacing.md, fontSize: 14, color: colors.text, minHeight: 100, borderWidth: 1, borderColor: colors.border },
  photoBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: spacing.sm, backgroundColor: colors.surface, padding: spacing.md, borderRadius: borderRadius.lg, borderWidth: 1, borderColor: colors.primary, borderStyle: 'dashed', marginTop: spacing.md },
  photoBtnText: { fontSize: 14, color: colors.primary, fontWeight: '500' },
  submitBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: spacing.sm, backgroundColor: colors.primary, padding: spacing.md, borderRadius: borderRadius.lg, marginTop: spacing.lg, marginBottom: spacing.xxl },
  submitBtnDisabled: { opacity: 0.5 },
  submitText: { color: '#fff', fontSize: 16, fontWeight: '700' },
  successContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: spacing.xl },
  successTitle: { fontSize: 24, fontWeight: '700', color: colors.primary, marginTop: spacing.md },
  successSub: { fontSize: 15, color: colors.textLight, textAlign: 'center', marginTop: spacing.sm },
  doneBtn: { backgroundColor: colors.primary, paddingHorizontal: spacing.xl, paddingVertical: spacing.md, borderRadius: borderRadius.lg, marginTop: spacing.xl },
  doneBtnText: { color: '#fff', fontSize: 16, fontWeight: '700' },
});

export default HazardReportScreen;
