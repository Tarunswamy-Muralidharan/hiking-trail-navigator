let checkInTimer = null;
let missedCount = 0;

export const startCheckInTimer = (intervalMinutes = 60, onCheckInDue) => {
  stopCheckInTimer();
  missedCount = 0;
  const intervalMs = intervalMinutes * 60 * 1000;
  checkInTimer = setInterval(() => {
    onCheckInDue(getEscalationLevel(missedCount));
    missedCount++;
  }, intervalMs);
  return true;
};

export const stopCheckInTimer = () => {
  if (checkInTimer) {
    clearInterval(checkInTimer);
    checkInTimer = null;
  }
  missedCount = 0;
};

export const acknowledgeCheckIn = () => {
  missedCount = 0;
  return { timestamp: new Date().toISOString(), status: 'safe' };
};

export const missedCheckIn = (userId, location) => {
  missedCount++;
  const level = getEscalationLevel(missedCount);
  return {
    userId,
    location,
    missedCount,
    escalationLevel: level,
    timestamp: new Date().toISOString(),
    shouldTriggerEmergency: level === 'emergency',
  };
};

export const getEscalationLevel = (missed) => {
  if (missed >= 3) return 'emergency';
  if (missed >= 2) return 'alert';
  return 'warning';
};

export default {
  startCheckInTimer, stopCheckInTimer, acknowledgeCheckIn,
  missedCheckIn, getEscalationLevel,
};
