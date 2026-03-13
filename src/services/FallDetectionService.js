import { Accelerometer } from 'expo-sensors';

let subscription = null;
let readings = [];
const WINDOW_SIZE = 50;
const FALL_THRESHOLD = 3.0;
const STILLNESS_THRESHOLD = 0.3;

export const startMonitoring = (onFallDetected) => {
  Accelerometer.setUpdateInterval(100);
  subscription = Accelerometer.addListener((data) => {
    const magnitude = Math.sqrt(data.x * data.x + data.y * data.y + data.z * data.z);
    readings.push({ magnitude, timestamp: Date.now() });
    if (readings.length > WINDOW_SIZE) readings.shift();
    if (detectFall(readings)) {
      onFallDetected({
        timestamp: new Date().toISOString(),
        magnitude,
        readings: [...readings],
      });
      readings = [];
    }
  });
  return true;
};

export const stopMonitoring = () => {
  if (subscription) {
    subscription.remove();
    subscription = null;
  }
  readings = [];
};

export const detectFall = (data) => {
  if (data.length < 20) return false;
  const recent = data.slice(-20);
  const impactIdx = recent.findIndex((r) => r.magnitude > FALL_THRESHOLD);
  if (impactIdx === -1 || impactIdx > recent.length - 5) return false;
  const afterImpact = recent.slice(impactIdx + 1);
  const avgAfter = afterImpact.reduce((s, r) => s + r.magnitude, 0) / afterImpact.length;
  return Math.abs(avgAfter - 1.0) < STILLNESS_THRESHOLD;
};

export const confirmFalseAlarm = () => {
  readings = [];
  return { status: 'dismissed', timestamp: new Date().toISOString() };
};

export default { startMonitoring, stopMonitoring, detectFall, confirmFalseAlarm };
