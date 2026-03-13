import { trails } from '../constants/trailData';
import { calculateDistance } from './LocationService';

export const searchTrails = (filters = {}) => {
  let results = [...trails];
  if (filters.difficulty) {
    results = results.filter((t) => t.difficulty === filters.difficulty);
  }
  if (filters.maxDistance) {
    results = results.filter((t) => t.distance <= filters.maxDistance);
  }
  if (filters.minRating) {
    results = results.filter((t) => t.rating >= filters.minRating);
  }
  if (filters.region) {
    results = results.filter((t) => t.region === filters.region);
  }
  if (filters.query) {
    const q = filters.query.toLowerCase();
    results = results.filter(
      (t) => t.name.toLowerCase().includes(q) || t.description.toLowerCase().includes(q) || t.region.toLowerCase().includes(q)
    );
  }
  return results;
};

export const getTrailById = (id) => {
  return trails.find((t) => t.id === id) || null;
};

export const getNearbyTrails = (location, radiusKm = 50) => {
  return trails.filter((t) => {
    const dist = calculateDistance(location, t.startPoint);
    return dist <= radiusKm * 1000;
  }).sort((a, b) => {
    const dA = calculateDistance(location, a.startPoint);
    const dB = calculateDistance(location, b.startPoint);
    return dA - dB;
  });
};

export const sortTrails = (trailList, sortBy = 'popularity') => {
  const sorted = [...trailList];
  switch (sortBy) {
    case 'distance': return sorted.sort((a, b) => a.distance - b.distance);
    case 'difficulty': {
      const order = { Easy: 1, Moderate: 2, Hard: 3, Expert: 4 };
      return sorted.sort((a, b) => order[a.difficulty] - order[b.difficulty]);
    }
    case 'rating': return sorted.sort((a, b) => b.rating - a.rating);
    case 'popularity': return sorted.sort((a, b) => b.popularity - a.popularity);
    default: return sorted;
  }
};

export const calculateEstimatedTime = (trail, pace = 'moderate') => {
  const multiplier = pace === 'fast' ? 0.8 : pace === 'slow' ? 1.3 : 1.0;
  return Math.round(trail.estimatedDuration * multiplier);
};

export default { searchTrails, getTrailById, getNearbyTrails, sortTrails, calculateEstimatedTime };
