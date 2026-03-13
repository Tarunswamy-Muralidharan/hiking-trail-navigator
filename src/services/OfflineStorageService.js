import AsyncStorage from '@react-native-async-storage/async-storage';

const KEYS = {
  TRAILS: 'offline_trails',
  HIKE: 'offline_hike',
  HAZARDS: 'offline_hazards',
  QUEUE: 'offline_queue',
  MAP_TILES: 'offline_map_tiles',
};

export const saveTrailData = async (trails) => {
  await AsyncStorage.setItem(KEYS.TRAILS, JSON.stringify(trails));
};

export const getTrailData = async () => {
  const data = await AsyncStorage.getItem(KEYS.TRAILS);
  return data ? JSON.parse(data) : null;
};

export const saveMapTiles = async (region) => {
  const tiles = { region, downloadedAt: new Date().toISOString(), size: '~50MB' };
  await AsyncStorage.setItem(KEYS.MAP_TILES, JSON.stringify(tiles));
  return tiles;
};

export const saveHikeData = async (hikeData) => {
  await AsyncStorage.setItem(KEYS.HIKE, JSON.stringify(hikeData));
};

export const getHikeData = async () => {
  const data = await AsyncStorage.getItem(KEYS.HIKE);
  return data ? JSON.parse(data) : null;
};

export const saveHazardReports = async (hazards) => {
  await AsyncStorage.setItem(KEYS.HAZARDS, JSON.stringify(hazards));
};

export const getHazardReports = async () => {
  const data = await AsyncStorage.getItem(KEYS.HAZARDS);
  return data ? JSON.parse(data) : [];
};

export const queueOfflineAction = async (action) => {
  const existing = await AsyncStorage.getItem(KEYS.QUEUE);
  const queue = existing ? JSON.parse(existing) : [];
  queue.push({ ...action, queuedAt: new Date().toISOString() });
  await AsyncStorage.setItem(KEYS.QUEUE, JSON.stringify(queue));
};

export const getQueuedActions = async () => {
  const data = await AsyncStorage.getItem(KEYS.QUEUE);
  return data ? JSON.parse(data) : [];
};

export const syncOfflineData = async () => {
  const actions = await getQueuedActions();
  if (actions.length === 0) return { synced: 0 };
  // In production, send each action to the server
  await AsyncStorage.removeItem(KEYS.QUEUE);
  return { synced: actions.length };
};

export const clearCache = async () => {
  await AsyncStorage.multiRemove(Object.values(KEYS));
};

export const getCacheSize = async () => {
  let total = 0;
  for (const key of Object.values(KEYS)) {
    const val = await AsyncStorage.getItem(key);
    if (val) total += val.length;
  }
  return { bytes: total, formatted: total > 1024 * 1024 ? (total / (1024 * 1024)).toFixed(1) + ' MB' : (total / 1024).toFixed(1) + ' KB' };
};

export default {
  saveTrailData, getTrailData, saveMapTiles, saveHikeData, getHikeData,
  saveHazardReports, getHazardReports, queueOfflineAction, getQueuedActions,
  syncOfflineData, clearCache, getCacheSize,
};
