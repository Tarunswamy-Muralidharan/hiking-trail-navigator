import React, { useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, TextInput, StyleSheet, Alert, Modal } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { Linking } from 'react-native';
import { useAppContext } from '../store/AppContext';
import { colors, spacing, borderRadius, shadows } from '../constants/theme';

const RELATIONS = ['Family', 'Friend', 'Guide', 'Other'];

const EmergencyContactsScreen = () => {
  const { state, dispatch } = useAppContext();
  const [showAdd, setShowAdd] = useState(false);
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [relation, setRelation] = useState('Family');
  const contacts = state.user.emergencyContacts;

  const addContact = () => {
    if (!name.trim() || !phone.trim()) {
      Alert.alert('Missing Info', 'Please enter name and phone number.');
      return;
    }
    if (contacts.length >= 5) {
      Alert.alert('Limit Reached', 'Maximum 5 emergency contacts allowed.');
      return;
    }
    const newContacts = [...contacts, { id: 'ec-' + Date.now(), name: name.trim(), phone: phone.trim(), relation }];
    dispatch({ type: 'UPDATE_EMERGENCY_CONTACTS', payload: newContacts });
    setShowAdd(false);
    setName('');
    setPhone('');
    setRelation('Family');
  };

  const deleteContact = (id) => {
    Alert.alert('Delete Contact', 'Remove this emergency contact?', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: () => {
        dispatch({ type: 'UPDATE_EMERGENCY_CONTACTS', payload: contacts.filter((c) => c.id !== id) });
      }},
    ]);
  };

  const renderContact = ({ item, index }) => (
    <View style={styles.contactCard}>
      <View style={[styles.priorityBadge, { backgroundColor: index === 0 ? colors.primary : colors.border }]}>
        <Text style={[styles.priorityText, { color: index === 0 ? '#fff' : colors.textLight }]}>{index + 1}</Text>
      </View>
      <View style={styles.contactInfo}>
        <Text style={styles.contactName}>{item.name}</Text>
        <Text style={styles.contactPhone}>{item.phone}</Text>
        <Text style={styles.contactRelation}>{item.relation}</Text>
      </View>
      <View style={styles.contactActions}>
        <TouchableOpacity onPress={() => Linking.openURL('tel:' + item.phone)} style={styles.iconBtn}>
          <Ionicons name="call" size={20} color={colors.primary} />
        </TouchableOpacity>
        <TouchableOpacity onPress={() => deleteContact(item.id)} style={styles.iconBtn}>
          <Ionicons name="trash-outline" size={20} color={colors.danger} />
        </TouchableOpacity>
      </View>
    </View>
  );

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <FlatList
        data={contacts}
        keyExtractor={(item) => item.id}
        renderItem={renderContact}
        contentContainerStyle={styles.list}
        ListHeaderComponent={
          <View style={styles.header}>
            <Text style={styles.headerText}>Emergency contacts receive alerts during SOS and missed check-ins.</Text>
          </View>
        }
        ListFooterComponent={
          contacts.length < 5 ? (
            <TouchableOpacity style={styles.addBtn} onPress={() => setShowAdd(true)}>
              <Ionicons name="add-circle" size={24} color={colors.primary} />
              <Text style={styles.addBtnText}>Add Emergency Contact</Text>
            </TouchableOpacity>
          ) : null
        }
      />

      {/* Add contact modal */}
      <Modal visible={showAdd} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>Add Emergency Contact</Text>
            <TextInput style={styles.input} placeholder="Full Name" value={name} onChangeText={setName} placeholderTextColor={colors.textLight} />
            <TextInput style={styles.input} placeholder="Phone Number" value={phone} onChangeText={setPhone} keyboardType="phone-pad" placeholderTextColor={colors.textLight} />
            <Text style={styles.inputLabel}>Relation</Text>
            <View style={styles.relationRow}>
              {RELATIONS.map((r) => (
                <TouchableOpacity key={r} style={[styles.relationBtn, relation === r && styles.relationBtnActive]} onPress={() => setRelation(r)}>
                  <Text style={[styles.relationText, relation === r && styles.relationTextActive]}>{r}</Text>
                </TouchableOpacity>
              ))}
            </View>
            <View style={styles.modalActions}>
              <TouchableOpacity style={styles.cancelBtn} onPress={() => setShowAdd(false)}>
                <Text style={styles.cancelBtnText}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.saveBtn} onPress={addContact}>
                <Text style={styles.saveBtnText}>Save</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  list: { padding: spacing.md },
  header: { backgroundColor: '#E3F2FD', padding: spacing.md, borderRadius: borderRadius.lg, marginBottom: spacing.md },
  headerText: { fontSize: 13, color: colors.info, lineHeight: 20 },
  contactCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surface, padding: spacing.md, borderRadius: borderRadius.lg, marginBottom: spacing.sm, ...shadows.small },
  priorityBadge: { width: 32, height: 32, borderRadius: 16, justifyContent: 'center', alignItems: 'center' },
  priorityText: { fontSize: 14, fontWeight: '700' },
  contactInfo: { flex: 1, marginLeft: spacing.sm },
  contactName: { fontSize: 16, fontWeight: '600', color: colors.text },
  contactPhone: { fontSize: 14, color: colors.textLight, marginTop: 2 },
  contactRelation: { fontSize: 12, color: colors.primary, marginTop: 2 },
  contactActions: { flexDirection: 'row', gap: spacing.sm },
  iconBtn: { padding: spacing.sm },
  addBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: spacing.sm, padding: spacing.md, borderRadius: borderRadius.lg, borderWidth: 1, borderColor: colors.primary, borderStyle: 'dashed', marginTop: spacing.sm },
  addBtnText: { fontSize: 15, color: colors.primary, fontWeight: '600' },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'flex-end' },
  modalCard: { backgroundColor: colors.surface, borderTopLeftRadius: borderRadius.xl, borderTopRightRadius: borderRadius.xl, padding: spacing.lg },
  modalTitle: { fontSize: 20, fontWeight: '700', color: colors.text, marginBottom: spacing.md },
  input: { backgroundColor: colors.background, borderRadius: borderRadius.md, padding: spacing.md, fontSize: 15, color: colors.text, marginBottom: spacing.sm },
  inputLabel: { fontSize: 14, fontWeight: '600', color: colors.text, marginBottom: spacing.sm },
  relationRow: { flexDirection: 'row', gap: spacing.sm, marginBottom: spacing.md },
  relationBtn: { flex: 1, padding: spacing.sm, borderRadius: borderRadius.md, backgroundColor: colors.background, alignItems: 'center' },
  relationBtnActive: { backgroundColor: colors.primary },
  relationText: { fontSize: 13, fontWeight: '600', color: colors.text },
  relationTextActive: { color: '#fff' },
  modalActions: { flexDirection: 'row', gap: spacing.sm },
  cancelBtn: { flex: 1, padding: spacing.md, borderRadius: borderRadius.lg, backgroundColor: colors.background, alignItems: 'center' },
  cancelBtnText: { fontSize: 15, fontWeight: '600', color: colors.textLight },
  saveBtn: { flex: 1, padding: spacing.md, borderRadius: borderRadius.lg, backgroundColor: colors.primary, alignItems: 'center' },
  saveBtnText: { fontSize: 15, fontWeight: '700', color: '#fff' },
});

export default EmergencyContactsScreen;
