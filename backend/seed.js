const bcrypt = require('bcryptjs');
const { getDb, initDatabase } = require('./config/database');

async function seed() {
  await initDatabase();
  const db = getDb();

  console.log('Seeding database...');

  // Users
  const users = [
    { name: 'Hiker Demo', email: 'hiker@demo.com', phone: '+91-9876543210', password: 'password123' },
    { name: 'Forest Ranger', email: 'ranger@forest.gov', phone: '+91-1800123456', password: 'ranger123' },
    { name: 'Trail Guide', email: 'guide@trails.com', phone: '+91-9876543211', password: 'guide123' },
  ];

  const insertUser = db.prepare('INSERT OR IGNORE INTO users (name, email, phone, password_hash) VALUES (?,?,?,?)');
  users.forEach((u) => {
    insertUser.run(u.name, u.email, u.phone, bcrypt.hashSync(u.password, 10));
  });

  // Trails
  const trails = [
    ['Kudremukh Peak Trail', 'Stunning trek through Western Ghats', 'Hard', 12.5, 420, 1150, 4.7, 13.2465, 75.254, 13.23, 75.24, 'Chikkamagaluru', 89, 'partial'],
    ['Mullayyanagiri Sunrise Trek', 'Highest peak in Karnataka at 1930m', 'Moderate', 8, 300, 780, 4.5, 13.392, 75.715, 13.381, 75.704, 'Chikkamagaluru', 92, 'partial'],
    ['Coorg Brahmagiri Trail', 'Ridge walk along Karnataka-Kerala border', 'Hard', 14, 480, 1020, 4.6, 12.115, 75.985, 12.105, 75.975, 'Coorg', 78, 'none'],
    ['Tadiandamol Easy Loop', 'Beginner-friendly loop with coffee plantations', 'Easy', 5.5, 150, 320, 4.2, 12.253, 75.732, 12.253, 75.732, 'Coorg', 95, 'full'],
    ['Kumara Parvatha Expedition', 'Toughest trek through Pushpagiri Sanctuary', 'Expert', 22, 720, 1580, 4.8, 12.456, 75.684, 12.43, 75.658, 'Dakshina Kannada', 72, 'none'],
    ['Netravati Peak Trail', 'Hidden gem with pristine grasslands', 'Moderate', 10, 360, 850, 4.4, 13.185, 75.22, 13.172, 75.208, 'Chikkamagaluru', 55, 'none'],
    ['Mandalpatti Jeep Trail', 'Scenic off-road to misty viewpoints', 'Easy', 6, 180, 420, 4.3, 12.395, 75.842, 12.3875, 75.8335, 'Coorg', 88, 'partial'],
    ['Ballalarayana Durga Fort Trek', 'Historical trek to ancient fort', 'Moderate', 9, 330, 720, 4.1, 13.305, 75.368, 13.296, 75.358, 'Chikkamagaluru', 63, 'partial'],
  ];

  const insertTrail = db.prepare('INSERT OR IGNORE INTO trails (name, description, difficulty, distance_km, estimated_duration_min, elevation_gain_m, rating, start_lat, start_lng, end_lat, end_lng, region, popularity, coverage_status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)');
  trails.forEach((t) => insertTrail.run(...t));

  // Danger zones
  const zones = [
    ['Kudremukh Elephant Corridor', 'wildlife', 'Active elephant migration corridor', 13.24, 75.247, 800, 'high', 'authority', 1],
    ['Brahmagiri Landslide Zone', 'landslide', 'Landslide-prone during monsoon', 12.108, 75.978, 500, 'critical', 'authority', 1],
    ['Pushpagiri Restricted Area', 'restricted', 'Wildlife sanctuary core zone', 12.45, 75.675, 1200, 'high', 'authority', 1],
    ['Netravati Stream Flood Zone', 'flood', 'Floods rapidly during heavy rains', 13.18, 75.214, 300, 'medium', 'community', 1],
    ['Ballalarayana Unstable Terrain', 'terrain', 'Rocky terrain with loose gravel', 13.297, 75.359, 400, 'medium', 'community', 1],
  ];

  const insertZone = db.prepare('INSERT OR IGNORE INTO danger_zones (name, type, description, center_lat, center_lng, radius_m, severity, source, verified) VALUES (?,?,?,?,?,?,?,?,?)');
  zones.forEach((z) => insertZone.run(...z));

  // Save to disk
  db.save();

  console.log('Database seeded successfully!');
  console.log('Demo login: hiker@demo.com / password123');
  process.exit(0);
}

seed().catch((err) => {
  console.error('Seed failed:', err);
  process.exit(1);
});
