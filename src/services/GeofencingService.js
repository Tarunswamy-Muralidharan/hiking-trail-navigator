let deviationInterval = null;

export const getDistanceFromTrail = (currentLocation, trailCoordinates) => {
  let minDist = Infinity;
  for (let i = 0; i < trailCoordinates.length - 1; i++) {
    const dist = pointToSegmentDistance(currentLocation, trailCoordinates[i], trailCoordinates[i + 1]);
    if (dist < minDist) minDist = dist;
  }
  return minDist;
};

export const checkDeviation = (currentLocation, trailCoordinates, bufferMeters = 100) => {
  const distance = getDistanceFromTrail(currentLocation, trailCoordinates);
  return {
    isDeviated: distance > bufferMeters,
    distance,
    severity: getDeviationSeverity(distance),
  };
};

export const startDeviationMonitoring = (trailCoordinates, getCurrentLocation, onDeviation, bufferMeters = 100) => {
  stopDeviationMonitoring();
  deviationInterval = setInterval(async () => {
    const loc = await getCurrentLocation();
    if (loc) {
      const result = checkDeviation(loc, trailCoordinates, bufferMeters);
      if (result.isDeviated) onDeviation(result);
    }
  }, 10000);
};

export const stopDeviationMonitoring = () => {
  if (deviationInterval) {
    clearInterval(deviationInterval);
    deviationInterval = null;
  }
};

export const getDeviationSeverity = (distanceMeters) => {
  if (distanceMeters > 500) return 'severe';
  if (distanceMeters > 200) return 'moderate';
  return 'minor';
};

const pointToSegmentDistance = (point, lineStart, lineEnd) => {
  const R = 6371e3;
  const toRad = (d) => (d * Math.PI) / 180;

  const d1 = haversine(point, lineStart);
  const d2 = haversine(point, lineEnd);
  const d3 = haversine(lineStart, lineEnd);

  if (d3 === 0) return d1;
  if (d1 * d1 > d2 * d2 + d3 * d3) return d2;
  if (d2 * d2 > d1 * d1 + d3 * d3) return d1;

  const s = (d1 + d2 + d3) / 2;
  const area = Math.sqrt(Math.max(0, s * (s - d1) * (s - d2) * (s - d3)));
  return (2 * area) / d3;
};

const haversine = (p1, p2) => {
  const R = 6371e3;
  const dLat = ((p2.latitude - p1.latitude) * Math.PI) / 180;
  const dLon = ((p2.longitude - p1.longitude) * Math.PI) / 180;
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos((p1.latitude * Math.PI) / 180) * Math.cos((p2.latitude * Math.PI) / 180) * Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
};

export default {
  getDistanceFromTrail, checkDeviation, startDeviationMonitoring,
  stopDeviationMonitoring, getDeviationSeverity,
};
