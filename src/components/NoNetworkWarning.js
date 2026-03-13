import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, spacing, borderRadius } from '../constants/theme';

const NoNetworkWarning = ({ visible, onDismiss, onEnableOffline }) => {
  if (!visible) return null;
  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Ionicons name="cellular-outline" size={24} color={colors.warning} />
        <Text style={styles.title}>Limited Coverage Area</Text>
        <TouchableOpacity onPress={onDismiss}>
          <Ionicons name="close" size={20} color={colors.textLight} />
        </TouchableOpacity>
      </View>
      <Text style={styles.message}>You are entering an area with limited or no cellular coverage.</Text>
      <View style={styles.tips}>
        <Text style={styles.tip}>* Download offline maps before proceeding</Text>
        <Text style={styles.tip}>* Enable safety monitoring features</Text>
        <Text style={styles.tip}>* Share your location with contacts now</Text>
      </View>
      <View style={styles.actions}>
        <TouchableOpacity style={styles.primaryBtn} onPress={onEnableOffline}>
          <Text style={styles.primaryBtnText}>Enable Offline Mode</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.secondaryBtn} onPress={onDismiss}>
          <Text style={styles.secondaryBtnText}>Dismiss</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { backgroundColor: '#FFF8E1', borderRadius: borderRadius.lg, padding: spacing.md, margin: spacing.md, borderWidth: 1, borderColor: colors.warning },
  header: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, marginBottom: spacing.sm },
  title: { flex: 1, fontSize: 16, fontWeight: '700', color: colors.text },
  message: { fontSize: 13, color: colors.text, lineHeight: 20, marginBottom: spacing.sm },
  tips: { marginBottom: spacing.md },
  tip: { fontSize: 12, color: colors.textLight, lineHeight: 20 },
  actions: { flexDirection: 'row', gap: spacing.sm },
  primaryBtn: { flex: 1, backgroundColor: colors.warning, padding: 10, borderRadius: borderRadius.md, alignItems: 'center' },
  primaryBtnText: { color: '#fff', fontWeight: '700', fontSize: 13 },
  secondaryBtn: { flex: 1, backgroundColor: colors.border, padding: 10, borderRadius: borderRadius.md, alignItems: 'center' },
  secondaryBtnText: { color: colors.text, fontWeight: '600', fontSize: 13 },
});

export default NoNetworkWarning;
