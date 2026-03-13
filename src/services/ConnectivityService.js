import * as Network from 'expo-network';

let monitorInterval = null;

export const startMonitoring = (onStatusChange) => {
  const check = async () => {
    const status = await getCurrentStatus();
    onStatusChange(status);
  };
  check();
  monitorInterval = setInterval(check, 10000);
};

export const stopMonitoring = () => {
  if (monitorInterval) {
    clearInterval(monitorInterval);
    monitorInterval = null;
  }
};

export const getCurrentStatus = async () => {
  try {
    const networkState = await Network.getNetworkStateAsync();
    return {
      isConnected: networkState.isConnected,
      type: networkState.type,
      isInternetReachable: networkState.isInternetReachable,
    };
  } catch (e) {
    return { isConnected: false, type: 'unknown', isInternetReachable: false };
  }
};

export const checkSignalStrength = async () => {
  const status = await getCurrentStatus();
  if (!status.isConnected) return 'none';
  if (!status.isInternetReachable) return 'weak';
  return 'good';
};

export const isInNoCoverageZone = (location, noCoverageZones) => {
  for (const zone of noCoverageZones) {
    const dist = getDistanceBetween(location, zone.center);
    if (dist <= zone.radius) return { inZone: true, zone };
  }
  return { inZone: false, zone: null };
};

const getDistanceBetween = (p1, p2) => {
  const R = 6371e3;
  const dLat = ((p2.latitude - p1.latitude) * Math.PI) / 180;
  const dLon = ((p2.longitude - p1.longitude) * Math.PI) / 180;
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos((p1.latitude * Math.PI) / 180) * Math.cos((p2.latitude * Math.PI) / 180) * Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
};

export default { startMonitoring, stopMonitoring, getCurrentStatus, checkSignalStrength, isInNoCoverageZone };
