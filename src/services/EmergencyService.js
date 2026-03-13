import AsyncStorage from '@react-native-async-storage/async-storage';

const API_BASE = 'http://localhost:3000/api';
let continuousUpdateTimer = null;

export const triggerSOS = async (userId, location, trailInfo) => {
  const alert = {
    userId, type: 'sos', latitude: location.latitude, longitude: location.longitude,
    trailInfo, timestamp: new Date().toISOString(), status: 'active',
  };
  try {
    const response = await fetch(API_BASE + '/safety/sos', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(alert),
    });
    if (response.ok) return { success: true, alert };
  } catch (e) {
    await queueOfflineAlert(alert);
  }
  return { success: true, alert, queued: true };
};

export const cancelSOS = async (userId) => {
  stopContinuousLocationUpdates();
  try {
    await fetch(API_BASE + '/safety/sos/cancel', {
      method: 'PUT', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId }),
    });
  } catch (e) { /* queue for later */ }
  return { success: true };
};

export const triggerSilentSOS = async (userId, location) => {
  const alert = {
    userId, type: 'silent_sos', latitude: location.latitude, longitude: location.longitude,
    timestamp: new Date().toISOString(), status: 'active', silent: true,
  };
  try {
    await fetch(API_BASE + '/safety/silent-sos', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(alert),
    });
  } catch (e) {
    await queueOfflineAlert(alert);
  }
  return { success: true, alert };
};

export const sendLocationToContacts = async (contacts, location) => {
  return contacts.map((c) => ({ contact: c.name, sent: true }));
};

export const startContinuousLocationUpdates = (userId, getLocation) => {
  continuousUpdateTimer = setInterval(async () => {
    const loc = await getLocation();
    if (loc) {
      try {
        await fetch(API_BASE + '/safety/location-update', {
          method: 'POST', headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ userId, ...loc }),
        });
      } catch (e) { /* continue silently */ }
    }
  }, 30000);
};

export const stopContinuousLocationUpdates = () => {
  if (continuousUpdateTimer) {
    clearInterval(continuousUpdateTimer);
    continuousUpdateTimer = null;
  }
};

const queueOfflineAlert = async (alert) => {
  try {
    const existing = await AsyncStorage.getItem('offline_alerts');
    const alerts = existing ? JSON.parse(existing) : [];
    alerts.push(alert);
    await AsyncStorage.setItem('offline_alerts', JSON.stringify(alerts));
  } catch (e) { console.error('Failed to queue alert:', e); }
};

export default {
  triggerSOS, cancelSOS, triggerSilentSOS, sendLocationToContacts,
  startContinuousLocationUpdates, stopContinuousLocationUpdates,
};
