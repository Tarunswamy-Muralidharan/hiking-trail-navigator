const express = require('express');
const { getDb } = require('../config/database');
const { verifyToken } = require('../middleware/auth');

const router = express.Router();

router.get('/', (req, res) => {
  try {
    const db = getDb();
    const { type, severity } = req.query;
    let sql = 'SELECT * FROM danger_zones WHERE 1=1';
    const params = [];
    if (type) { sql += ' AND type = ?'; params.push(type); }
    if (severity) { sql += ' AND severity = ?'; params.push(severity); }
    sql += ' ORDER BY updated_at DESC';
    const zones = db.prepare(sql).all(...params);
    res.json({ success: true, data: zones });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.post('/', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const { name, type, description, center_lat, center_lng, radius_m, severity } = req.body;
    if (!name || !center_lat || !center_lng || !radius_m) {
      return res.status(400).json({ success: false, error: 'Name, center coordinates, and radius required' });
    }
    const result = db.prepare(
      'INSERT INTO danger_zones (name, type, description, center_lat, center_lng, radius_m, severity, source) VALUES (?,?,?,?,?,?,?,?)'
    ).run(name, type, description, center_lat, center_lng, radius_m, severity || 'medium', 'community');
    res.status(201).json({ success: true, data: { id: result.lastInsertRowid } });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.get('/check', (req, res) => {
  try {
    const db = getDb();
    const { lat, lng } = req.query;
    if (!lat || !lng) return res.status(400).json({ success: false, error: 'Latitude and longitude required' });
    const zones = db.prepare('SELECT * FROM danger_zones').all();
    const inZones = zones.filter((z) => {
      const dist = haversine(parseFloat(lat), parseFloat(lng), z.center_lat, z.center_lng);
      return dist <= z.radius_m;
    });
    res.json({ success: true, data: { inDangerZone: inZones.length > 0, zones: inZones } });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

function haversine(lat1, lng1, lat2, lng2) {
  const R = 6371e3;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLng = (lng2 - lng1) * Math.PI / 180;
  const a = Math.sin(dLat/2)**2 + Math.cos(lat1*Math.PI/180) * Math.cos(lat2*Math.PI/180) * Math.sin(dLng/2)**2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
}

module.exports = router;
