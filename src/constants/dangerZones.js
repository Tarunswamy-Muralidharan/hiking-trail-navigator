export const dangerZones = [
  {
    id: 'dz-001', name: 'Kudremukh Elephant Corridor', type: 'wildlife',
    description: 'Active elephant migration corridor. Herds frequently spotted Oct-Mar.',
    center: { latitude: 13.2400, longitude: 75.2470 }, radius: 800,
    severity: 'high', source: 'authority', verified: true, reportCount: 45, lastUpdated: '2026-02-15',
  },
  {
    id: 'dz-002', name: 'Brahmagiri Landslide Zone', type: 'landslide',
    description: 'Area prone to landslides during monsoon. Soil erosion has weakened terrain.',
    center: { latitude: 12.1080, longitude: 75.9780 }, radius: 500,
    severity: 'critical', source: 'authority', verified: true, reportCount: 32, lastUpdated: '2026-01-20',
  },
  {
    id: 'dz-003', name: 'Pushpagiri Restricted Area', type: 'restricted',
    description: 'Wildlife sanctuary core zone. Entry restricted without permit.',
    center: { latitude: 12.4500, longitude: 75.6750 }, radius: 1200,
    severity: 'high', source: 'authority', verified: true, reportCount: 0, lastUpdated: '2026-01-01',
  },
  {
    id: 'dz-004', name: 'Netravati Stream Flood Zone', type: 'flood',
    description: 'Stream crossing that floods rapidly during heavy rains.',
    center: { latitude: 13.1800, longitude: 75.2140 }, radius: 300,
    severity: 'medium', source: 'community', verified: true, reportCount: 18, lastUpdated: '2026-02-28',
  },
  {
    id: 'dz-005', name: 'Ballalarayana Unstable Terrain', type: 'terrain',
    description: 'Rocky terrain with loose gravel near fort ruins.',
    center: { latitude: 13.2970, longitude: 75.3590 }, radius: 400,
    severity: 'medium', source: 'community', verified: true, reportCount: 12, lastUpdated: '2026-03-01',
  },
  {
    id: 'dz-006', name: 'Kumara Parvatha Snake Zone', type: 'wildlife',
    description: 'High density of venomous snakes including King Cobras.',
    center: { latitude: 12.4420, longitude: 75.6690 }, radius: 600,
    severity: 'high', source: 'community', verified: false, reportCount: 8, lastUpdated: '2026-02-10',
  },
];

export const noCoverageZones = [
  { id: 'nc-001', name: 'Kudremukh Deep Forest', center: { latitude: 13.2350, longitude: 75.2430 }, radius: 2000, description: 'No cellular coverage.' },
  { id: 'nc-002', name: 'Brahmagiri Core Zone', center: { latitude: 12.1050, longitude: 75.9740 }, radius: 3000, description: 'Very limited to no coverage.' },
  { id: 'nc-003', name: 'Kumara Parvatha Summit', center: { latitude: 12.4300, longitude: 75.6580 }, radius: 2500, description: 'No coverage beyond Mantapa.' },
  { id: 'nc-004', name: 'Netravati Valley', center: { latitude: 13.1750, longitude: 75.2100 }, radius: 1500, description: 'Intermittent, mostly unavailable.' },
];

export const unexploredAreas = [
  { id: 'ue-001', name: 'Western Kudremukh Ridge', center: { latitude: 13.2280, longitude: 75.2350 }, radius: 1500, activityLevel: 'very_low' },
  { id: 'ue-002', name: 'South Brahmagiri Forest', center: { latitude: 12.0920, longitude: 75.9650 }, radius: 2000, activityLevel: 'very_low' },
  { id: 'ue-003', name: 'Inner Pushpagiri Range', center: { latitude: 12.4600, longitude: 75.6900 }, radius: 1800, activityLevel: 'low' },
];

export const getSeverityColor = (severity) => {
  switch (severity) {
    case 'low': return 'rgba(249, 168, 37, 0.3)';
    case 'medium': return 'rgba(255, 152, 0, 0.35)';
    case 'high': return 'rgba(244, 67, 54, 0.35)';
    case 'critical': return 'rgba(183, 28, 28, 0.4)';
    default: return 'rgba(158, 158, 158, 0.3)';
  }
};

export const getTypeIcon = (type) => {
  switch (type) {
    case 'wildlife': return 'paw';
    case 'terrain': return 'alert-circle';
    case 'landslide': return 'warning';
    case 'restricted': return 'lock-closed';
    case 'flood': return 'water';
    default: return 'help-circle';
  }
};

export default { dangerZones, noCoverageZones, unexploredAreas };
