const express = require('express');
const { getDb } = require('../config/database');
const { verifyToken, optionalAuth } = require('../middleware/auth');

const router = express.Router();

router.get('/', (req, res) => {
  try {
    const db = getDb();
    const { type, severity, trail_id, verified } = req.query;
    let sql = 'SELECT h.*, u.name as reporter_name FROM hazards h LEFT JOIN users u ON h.reporter_id = u.id WHERE h.status = "active"';
    const params = [];
    if (type) { sql += ' AND h.type = ?'; params.push(type); }
    if (severity) { sql += ' AND h.severity = ?'; params.push(severity); }
    if (trail_id) { sql += ' AND h.trail_id = ?'; params.push(trail_id); }
    if (verified !== undefined) { sql += ' AND h.verified = ?'; params.push(parseInt(verified)); }
    sql += ' ORDER BY h.created_at DESC';
    const hazards = db.prepare(sql).all(...params);
    res.json({ success: true, data: hazards });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.post('/', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const { trail_id, type, severity, description, latitude, longitude } = req.body;
    if (!type || !description) return res.status(400).json({ success: false, error: 'Type and description required' });
    const expires = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString();
    const result = db.prepare(
      'INSERT INTO hazards (trail_id, reporter_id, type, severity, description, latitude, longitude, expires_at) VALUES (?,?,?,?,?,?,?,?)'
    ).run(trail_id || null, req.user.id, type, severity || 'medium', description, latitude, longitude, expires);
    res.status(201).json({ success: true, data: { id: result.lastInsertRowid }, message: 'Hazard reported' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.post('/:id/validate', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const { confirmed } = req.body;
    db.prepare('INSERT OR REPLACE INTO hazard_validations (hazard_id, user_id, confirmed) VALUES (?, ?, ?)').run(req.params.id, req.user.id, confirmed ? 1 : 0);
    const count = db.prepare('SELECT COUNT(*) as cnt FROM hazard_validations WHERE hazard_id = ? AND confirmed = 1').get(req.params.id);
    db.prepare('UPDATE hazards SET confirmation_count = ?, verified = ? WHERE id = ?').run(count.cnt, count.cnt >= 3 ? 1 : 0, req.params.id);
    res.json({ success: true, message: 'Validation recorded', data: { confirmations: count.cnt } });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
