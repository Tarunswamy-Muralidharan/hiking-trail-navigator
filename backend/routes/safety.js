const express = require('express');
const { getDb } = require('../config/database');
const { verifyToken } = require('../middleware/auth');

const router = express.Router();

router.post('/sos', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const { latitude, longitude, trailInfo } = req.body;
    const result = db.prepare(
      'INSERT INTO emergency_alerts (user_id, type, latitude, longitude, trail_id, status, message) VALUES (?, ?, ?, ?, ?, ?, ?)'
    ).run(req.user.id, 'sos', latitude, longitude, trailInfo?.trailId || null, 'active', 'SOS triggered by user');
    res.status(201).json({ success: true, data: { alertId: result.lastInsertRowid }, message: 'SOS alert sent' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.put('/sos/:id/cancel', verifyToken, (req, res) => {
  try {
    const db = getDb();
    db.prepare('UPDATE emergency_alerts SET status = ?, resolved_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?').run('cancelled', req.params.id, req.user.id);
    res.json({ success: true, message: 'SOS cancelled' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.post('/silent-sos', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const { latitude, longitude } = req.body;
    const result = db.prepare(
      'INSERT INTO emergency_alerts (user_id, type, latitude, longitude, status, message) VALUES (?, ?, ?, ?, ?, ?)'
    ).run(req.user.id, 'silent_sos', latitude, longitude, 'active', 'Silent SOS');
    res.status(201).json({ success: true, data: { alertId: result.lastInsertRowid } });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.post('/checkin', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const { latitude, longitude, activity_id } = req.body;
    db.prepare('INSERT INTO safety_checkins (user_id, activity_id, status, latitude, longitude) VALUES (?, ?, ?, ?, ?)')
      .run(req.user.id, activity_id || null, 'safe', latitude, longitude);
    res.json({ success: true, message: 'Check-in recorded' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.get('/alerts', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const alerts = db.prepare('SELECT * FROM emergency_alerts WHERE user_id = ? ORDER BY created_at DESC LIMIT 20').all(req.user.id);
    res.json({ success: true, data: alerts });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.get('/alerts/active', (req, res) => {
  try {
    const db = getDb();
    const alerts = db.prepare('SELECT ea.*, u.name, u.phone FROM emergency_alerts ea JOIN users u ON ea.user_id = u.id WHERE ea.status = "active" ORDER BY ea.created_at DESC').all();
    res.json({ success: true, data: alerts });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
