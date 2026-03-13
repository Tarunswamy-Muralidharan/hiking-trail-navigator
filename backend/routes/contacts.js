const express = require('express');
const { getDb } = require('../config/database');
const { verifyToken } = require('../middleware/auth');

const router = express.Router();

router.get('/', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const contacts = db.prepare('SELECT * FROM emergency_contacts WHERE user_id = ? ORDER BY priority ASC').all(req.user.id);
    res.json({ success: true, data: contacts });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.post('/', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const count = db.prepare('SELECT COUNT(*) as cnt FROM emergency_contacts WHERE user_id = ?').get(req.user.id);
    if (count.cnt >= 5) return res.status(400).json({ success: false, error: 'Maximum 5 contacts allowed' });

    const { name, phone, relation } = req.body;
    if (!name || !phone) return res.status(400).json({ success: false, error: 'Name and phone required' });

    const result = db.prepare('INSERT INTO emergency_contacts (user_id, name, phone, relation, priority) VALUES (?,?,?,?,?)').run(req.user.id, name, phone, relation || 'Other', count.cnt);
    res.status(201).json({ success: true, data: { id: result.lastInsertRowid } });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.delete('/:id', verifyToken, (req, res) => {
  try {
    const db = getDb();
    db.prepare('DELETE FROM emergency_contacts WHERE id = ? AND user_id = ?').run(req.params.id, req.user.id);
    res.json({ success: true, message: 'Contact deleted' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
