import * as Location from 'expo-location';

let watchSubscription = null;

export const requestPermissions = async () => {
  const { status: foreground } = await Location.requestForegroundPermissionsAsync();
  if (foreground !== 'granted') return false;
  const { status: background } = await Location.requestBackgroundPermissionsAsync();
  return foreground === 'granted';
};

export const getCurrentLocation = async () => {
  try {
    const location = await Location.getCurrentPositionAsync({
      accuracy: Location.Accuracy.High,
    });
    return {
      latitude: location.coords.latitude,
      longitude: location.coords.longitude,
      altitude: location.coords.altitude,
      speed: location.coords.speed,
      accuracy: location.coords.accuracy,
      timestamp: location.timestamp,
    };
  } catch (error) {
    console.error('Error getting location:', error);
    return null;
  }
};

export const startTracking = async (callback, interval = 5000) => {
  try {
    watchSubscription = await Location.watchPositionAsync(
      {
        accuracy: Location.Accuracy.High,
        timeInterval: interval,
        distanceInterval: 5,
      },
      (location) => {
        callback({
          latitude: location.coords.latitude,
          longitude: location.coords.longitude,
          altitude: location.coords.altitude,
          speed: location.coords.speed,
          accuracy: location.coords.accuracy,
          timestamp: location.timestamp,
        });
      }
    );
    return true;
  } catch (error) {
    console.error('Error starting tracking:', error);
    return false;
  }
};

export const stopTracking = () => {
  if (watchSubscription) {
    watchSubscription.remove();
    watchSubscription = null;
  }
};

export const calculateDistance = (point1, point2) => {
  const R = 6371e3;
  const lat1 = (point1.latitude * Math.PI) / 180;
  const lat2 = (point2.latitude * Math.PI) / 180;
  const dLat = ((point2.latitude - point1.latitude) * Math.PI) / 180;
  const dLon = ((point2.longitude - point1.longitude) * Math.PI) / 180;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

export const calculateTotalDistance = (routePoints) => {
  let total = 0;
  for (let i = 1; i < routePoints.length; i++) {
    total += calculateDistance(routePoints[i - 1], routePoints[i]);
  }
  return total;
};

export const calculateElevationGain = (routePoints) => {
  let gain = 0;
  for (let i = 1; i < routePoints.length; i++) {
    const diff = (routePoints[i].altitude || 0) - (routePoints[i - 1].altitude || 0);
    if (diff > 0) gain += diff;
  }
  return gain;
};

export const isWithinRadius = (point, center, radiusMeters) => {
  return calculateDistance(point, center) <= radiusMeters;
};

export default {
  requestPermissions, getCurrentLocation, startTracking, stopTracking,
  calculateDistance, calculateTotalDistance, calculateElevationGain, isWithinRadius,
};
