export const trails = [
  {
    id: 'trail-001', name: 'Kudremukh Peak Trail',
    description: 'A stunning trek through the Western Ghats with panoramic views of rolling green hills.',
    difficulty: 'Hard', distance: 12.5, estimatedDuration: 420,
    elevationGain: 1150, rating: 4.7,
    coordinates: [{ latitude: 13.2465, longitude: 75.254 }, { latitude: 13.245, longitude: 75.253 }, { latitude: 13.243, longitude: 75.251 }, { latitude: 13.241, longitude: 75.2495 }, { latitude: 13.239, longitude: 75.248 }, { latitude: 13.237, longitude: 75.246 }, { latitude: 13.234, longitude: 75.243 }, { latitude: 13.232, longitude: 75.242 }, { latitude: 13.23, longitude: 75.24 }],
    startPoint: { latitude: 13.2465, longitude: 75.254 },
    endPoint: { latitude: 13.23, longitude: 75.24 },
    hazards: ['Steep sections', 'Leech-prone during monsoon'],
    region: 'Chikkamagaluru', popularity: 89, coverageStatus: 'partial',
    elevationProfile: [{ distance: 0, elevation: 950 }, { distance: 1.5, elevation: 1050 }, { distance: 3, elevation: 1200 }, { distance: 5, elevation: 1400 }, { distance: 7, elevation: 1600 }, { distance: 9, elevation: 1750 }, { distance: 11, elevation: 1850 }, { distance: 12.5, elevation: 1894 }],
  },
  {
    id: 'trail-002', name: 'Mullayyanagiri Sunrise Trek',
    description: 'Trek to the highest peak in Karnataka at 1930m with breathtaking sunrise views.',
    difficulty: 'Moderate', distance: 8, estimatedDuration: 300,
    elevationGain: 780, rating: 4.5,
    coordinates: [{ latitude: 13.392, longitude: 75.715 }, { latitude: 13.3905, longitude: 75.7135 }, { latitude: 13.389, longitude: 75.712 }, { latitude: 13.387, longitude: 75.71 }, { latitude: 13.3855, longitude: 75.7085 }, { latitude: 13.384, longitude: 75.707 }, { latitude: 13.381, longitude: 75.704 }],
    startPoint: { latitude: 13.392, longitude: 75.715 },
    endPoint: { latitude: 13.381, longitude: 75.704 },
    hazards: ['Fog reduces visibility', 'Cold temperatures at summit'],
    region: 'Chikkamagaluru', popularity: 92, coverageStatus: 'partial',
    elevationProfile: [{ distance: 0, elevation: 1150 }, { distance: 2, elevation: 1350 }, { distance: 4, elevation: 1550 }, { distance: 6, elevation: 1750 }, { distance: 8, elevation: 1930 }],
  },
  {
    id: 'trail-003', name: 'Coorg Brahmagiri Trail',
    description: 'A scenic ridge walk along the Karnataka-Kerala border through biodiversity-rich rainforests.',
    difficulty: 'Hard', distance: 14, estimatedDuration: 480,
    elevationGain: 1020, rating: 4.6,
    coordinates: [{ latitude: 12.115, longitude: 75.985 }, { latitude: 12.113, longitude: 75.983 }, { latitude: 12.111, longitude: 75.981 }, { latitude: 12.109, longitude: 75.979 }, { latitude: 12.107, longitude: 75.977 }, { latitude: 12.105, longitude: 75.975 }],
    startPoint: { latitude: 12.115, longitude: 75.985 },
    endPoint: { latitude: 12.105, longitude: 75.975 },
    hazards: ['Wildlife (elephants)', 'Slippery terrain in rain'],
    region: 'Coorg', popularity: 78, coverageStatus: 'none',
    elevationProfile: [{ distance: 0, elevation: 850 }, { distance: 3, elevation: 1100 }, { distance: 7, elevation: 1350 }, { distance: 11, elevation: 1500 }, { distance: 14, elevation: 1608 }],
  },
  {
    id: 'trail-004', name: 'Tadiandamol Easy Loop',
    description: 'Beginner-friendly loop trail with gentle gradients and coffee plantations.',
    difficulty: 'Easy', distance: 5.5, estimatedDuration: 150,
    elevationGain: 320, rating: 4.2,
    coordinates: [{ latitude: 12.253, longitude: 75.732 }, { latitude: 12.252, longitude: 75.734 }, { latitude: 12.251, longitude: 75.736 }, { latitude: 12.25, longitude: 75.735 }, { latitude: 12.249, longitude: 75.733 }, { latitude: 12.253, longitude: 75.732 }],
    startPoint: { latitude: 12.253, longitude: 75.732 },
    endPoint: { latitude: 12.253, longitude: 75.732 },
    hazards: [],
    region: 'Coorg', popularity: 95, coverageStatus: 'full',
    elevationProfile: [{ distance: 0, elevation: 1100 }, { distance: 1.5, elevation: 1250 }, { distance: 3, elevation: 1420 }, { distance: 4.5, elevation: 1300 }, { distance: 5.5, elevation: 1100 }],
  },
  {
    id: 'trail-005', name: 'Kumara Parvatha Expedition',
    description: 'One of the toughest treks in Karnataka through Pushpagiri Wildlife Sanctuary.',
    difficulty: 'Expert', distance: 22, estimatedDuration: 720,
    elevationGain: 1580, rating: 4.8,
    coordinates: [{ latitude: 12.456, longitude: 75.684 }, { latitude: 12.451, longitude: 75.679 }, { latitude: 12.445, longitude: 75.673 }, { latitude: 12.439, longitude: 75.667 }, { latitude: 12.43, longitude: 75.658 }],
    startPoint: { latitude: 12.456, longitude: 75.684 },
    endPoint: { latitude: 12.43, longitude: 75.658 },
    hazards: ['Steep rock climbing', 'No water after Mantapa', 'Wildlife zone'],
    region: 'Dakshina Kannada', popularity: 72, coverageStatus: 'none',
    elevationProfile: [{ distance: 0, elevation: 820 }, { distance: 5, elevation: 1050 }, { distance: 10, elevation: 1200 }, { distance: 15, elevation: 1400 }, { distance: 20, elevation: 1700 }, { distance: 22, elevation: 1712 }],
  },
  {
    id: 'trail-006', name: 'Netravati Peak Trail',
    description: 'A hidden gem near Kudremukh with pristine grasslands and river Netravati source.',
    difficulty: 'Moderate', distance: 10, estimatedDuration: 360,
    elevationGain: 850, rating: 4.4,
    coordinates: [{ latitude: 13.185, longitude: 75.22 }, { latitude: 13.182, longitude: 75.217 }, { latitude: 13.178, longitude: 75.213 }, { latitude: 13.172, longitude: 75.208 }],
    startPoint: { latitude: 13.185, longitude: 75.22 },
    endPoint: { latitude: 13.172, longitude: 75.208 },
    hazards: ['Stream crossings', 'Leeches during monsoon'],
    region: 'Chikkamagaluru', popularity: 55, coverageStatus: 'none',
    elevationProfile: [{ distance: 0, elevation: 900 }, { distance: 2.5, elevation: 1100 }, { distance: 5, elevation: 1350 }, { distance: 7.5, elevation: 1550 }, { distance: 10, elevation: 1680 }],
  },
  {
    id: 'trail-007', name: 'Mandalpatti Jeep Trail',
    description: 'A scenic off-road trail leading to misty viewpoints above the clouds.',
    difficulty: 'Easy', distance: 6, estimatedDuration: 180,
    elevationGain: 420, rating: 4.3,
    coordinates: [{ latitude: 12.395, longitude: 75.842 }, { latitude: 12.392, longitude: 75.838 }, { latitude: 12.389, longitude: 75.835 }, { latitude: 12.3875, longitude: 75.8335 }],
    startPoint: { latitude: 12.395, longitude: 75.842 },
    endPoint: { latitude: 12.3875, longitude: 75.8335 },
    hazards: ['Muddy roads in monsoon'],
    region: 'Coorg', popularity: 88, coverageStatus: 'partial',
    elevationProfile: [{ distance: 0, elevation: 1050 }, { distance: 2, elevation: 1200 }, { distance: 4, elevation: 1380 }, { distance: 6, elevation: 1450 }],
  },
  {
    id: 'trail-008', name: 'Ballalarayana Durga Fort Trek',
    description: 'A historical trek to an ancient fort atop a hill in the Western Ghats.',
    difficulty: 'Moderate', distance: 9, estimatedDuration: 330,
    elevationGain: 720, rating: 4.1,
    coordinates: [{ latitude: 13.305, longitude: 75.368 }, { latitude: 13.302, longitude: 75.365 }, { latitude: 13.299, longitude: 75.361 }, { latitude: 13.296, longitude: 75.358 }],
    startPoint: { latitude: 13.305, longitude: 75.368 },
    endPoint: { latitude: 13.296, longitude: 75.358 },
    hazards: ['Ruins can be slippery', 'Limited shade on summit'],
    region: 'Chikkamagaluru', popularity: 63, coverageStatus: 'partial',
    elevationProfile: [{ distance: 0, elevation: 780 }, { distance: 2, elevation: 950 }, { distance: 4.5, elevation: 1150 }, { distance: 7, elevation: 1350 }, { distance: 9, elevation: 1500 }],
  },
];

export const getDifficultyColor = (difficulty) => {
  switch (difficulty) {
    case 'Easy': return '#4CAF50';
    case 'Moderate': return '#FF9800';
    case 'Hard': return '#F44336';
    case 'Expert': return '#9C27B0';
    default: return '#757575';
  }
};

export const formatDuration = (minutes) => {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (hours === 0) return mins + 'min';
  if (mins === 0) return hours + 'h';
  return hours + 'h ' + mins + 'm';
};

export default trails;