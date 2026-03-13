import React from 'react';
import { View, Text, ActivityIndicator, Modal, StyleSheet } from 'react-native';
import { colors, spacing } from '../constants/theme';

const LoadingOverlay = ({ visible, message = 'Loading...' }) => (
  <Modal visible={visible} transparent animationType="fade">
    <View style={styles.overlay}>
      <View style={styles.box}>
        <ActivityIndicator size="large" color={colors.primary} />
        {message && <Text style={styles.message}>{message}</Text>}
      </View>
    </View>
  </Modal>
);

const styles = StyleSheet.create({
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', alignItems: 'center' },
  box: { backgroundColor: colors.surface, borderRadius: 16, padding: spacing.lg, alignItems: 'center', minWidth: 150 },
  message: { marginTop: spacing.md, fontSize: 14, color: colors.textLight },
});

export default LoadingOverlay;
