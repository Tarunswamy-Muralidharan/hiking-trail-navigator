const express = require('express');
const { getDb } = require('../config/database');
const { optionalAuth } = require('../middleware/auth');

const router = express.Router();

router.get('/', (req, res) => {
  try {
    const db = getDb();
    const { difficulty, region, sort = 'popularity', limit = 50 } = req.query;
    let sql = 'SELECT * FROM trails WHERE 1=1';
    const params = [];
    if (difficulty) { sql += ' AND difficulty = ?'; params.push(difficulty); }
    if (region) { sql += ' AND region = ?'; params.push(region); }
    const sortMap = { popularity: 'popularity DESC', distance: 'distance_km ASC', rating: 'rating DESC', difficulty: 'distance_km ASC' };
    sql += ' ORDER BY ' + (sortMap[sort] || 'popularity DESC');
    sql += ' LIMIT ?';
    params.push(parseInt(limit));
    const trails = db.prepare(sql).all(...params);
    res.json({ success: true, data: trails });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.get('/:id', (req, res) => {
  try {
    const db = getDb();
    const trail = db.prepare('SELECT * FROM trails WHERE id = ?').get(req.params.id);
    if (!trail) return res.status(404).json({ success: false, error: 'Trail not found' });
    const coordinates = db.prepare('SELECT latitude, longitude, altitude FROM trail_coordinates WHERE trail_id = ? ORDER BY sequence').all(trail.id);
    const hazards = db.prepare('SELECT * FROM hazards WHERE trail_id = ? AND status = "active"').all(trail.id);
    res.json({ success: true, data: { ...trail, coordinates, hazards } });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.get('/:id/reviews', (req, res) => {
  try {
    const db = getDb();
    const reviews = db.prepare('SELECT r.*, u.name as reviewer_name FROM trail_reviews r JOIN users u ON r.user_id = u.id WHERE r.trail_id = ? ORDER BY r.created_at DESC').all(req.params.id);
    res.json({ success: true, data: reviews });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
