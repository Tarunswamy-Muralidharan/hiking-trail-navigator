export const calculateTrailRisk = (trail, weatherData, hazards = [], dangerZones = [], connectivity = {}) => {
  let score = 0;

  // Difficulty factor
  const diffScores = { Easy: 10, Moderate: 25, Hard: 45, Expert: 70 };
  score += diffScores[trail.difficulty] || 25;

  // Weather factor
  if (weatherData) {
    if (weatherData.rainProbability > 60) score += 20;
    else if (weatherData.rainProbability > 30) score += 10;
    if (weatherData.windSpeed > 30) score += 15;
  }

  // Hazard factor
  score += Math.min(hazards.length * 8, 30);

  // Danger zone factor
  score += Math.min(dangerZones.length * 12, 25);

  // Connectivity factor
  if (trail.coverageStatus === 'none') score += 15;
  else if (trail.coverageStatus === 'partial') score += 8;

  return Math.min(score, 100);
};

export const getRiskLevel = (score) => {
  if (score >= 75) return 'critical';
  if (score >= 50) return 'high';
  if (score >= 25) return 'medium';
  return 'low';
};

export const getRiskFactors = (trail, conditions = {}) => {
  const factors = [];
  if (trail.difficulty === 'Hard' || trail.difficulty === 'Expert') {
    factors.push({ type: 'difficulty', label: 'High difficulty trail', severity: 'warning' });
  }
  if (trail.coverageStatus === 'none') {
    factors.push({ type: 'connectivity', label: 'No cellular coverage', severity: 'warning' });
  }
  if (trail.hazards && trail.hazards.length > 0) {
    factors.push({ type: 'hazards', label: trail.hazards.length + ' known hazards', severity: 'caution' });
  }
  if (conditions.rainProbability > 50) {
    factors.push({ type: 'weather', label: 'Rain expected', severity: 'warning' });
  }
  if (trail.distance > 15) {
    factors.push({ type: 'distance', label: 'Long distance trail', severity: 'info' });
  }
  return factors;
};

export const shouldEnhanceSafetyMonitoring = (riskLevel) => {
  return riskLevel === 'high' || riskLevel === 'critical';
};

export const getRecommendedCheckInInterval = (riskLevel) => {
  switch (riskLevel) {
    case 'critical': return 15;
    case 'high': return 30;
    case 'medium': return 60;
    default: return 60;
  }
};

export default {
  calculateTrailRisk, getRiskLevel, getRiskFactors,
  shouldEnhanceSafetyMonitoring, getRecommendedCheckInInterval,
};
