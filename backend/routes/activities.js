const express = require('express');
const { getDb } = require('../config/database');
const { verifyToken } = require('../middleware/auth');

const router = express.Router();

router.post('/', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const { trail_id, start_time, end_time, distance_km, duration_min, elevation_gain_m, avg_pace, route_data } = req.body;
    const result = db.prepare(
      'INSERT INTO activities (user_id, trail_id, start_time, end_time, distance_km, duration_min, elevation_gain_m, avg_pace, route_data) VALUES (?,?,?,?,?,?,?,?,?)'
    ).run(req.user.id, trail_id, start_time, end_time, distance_km, duration_min, elevation_gain_m, avg_pace, route_data ? JSON.stringify(route_data) : null);
    res.status(201).json({ success: true, data: { id: result.lastInsertRowid } });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.get('/', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const { page = 1, limit = 20 } = req.query;
    const offset = (parseInt(page) - 1) * parseInt(limit);
    const activities = db.prepare('SELECT a.*, t.name as trail_name FROM activities a LEFT JOIN trails t ON a.trail_id = t.id WHERE a.user_id = ? ORDER BY a.created_at DESC LIMIT ? OFFSET ?').all(req.user.id, parseInt(limit), offset);
    res.json({ success: true, data: activities });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.get('/stats', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const stats = db.prepare('SELECT COUNT(*) as total_hikes, COALESCE(SUM(distance_km),0) as total_distance, COALESCE(SUM(duration_min),0) as total_duration, COALESCE(SUM(elevation_gain_m),0) as total_elevation FROM activities WHERE user_id = ?').get(req.user.id);
    res.json({ success: true, data: stats });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
