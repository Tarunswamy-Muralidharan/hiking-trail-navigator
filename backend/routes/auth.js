const express = require('express');
const bcrypt = require('bcryptjs');
const { getDb } = require('../config/database');
const { verifyToken, generateToken } = require('../middleware/auth');

const router = express.Router();

router.post('/register', (req, res) => {
  try {
    const { name, email, phone, password } = req.body;
    if (!name || !email || !password) {
      return res.status(400).json({ success: false, error: 'Name, email, and password are required' });
    }
    const db = getDb();
    const existing = db.prepare('SELECT id FROM users WHERE email = ?').get(email);
    if (existing) return res.status(409).json({ success: false, error: 'Email already registered' });

    const hash = bcrypt.hashSync(password, 10);
    const result = db.prepare('INSERT INTO users (name, email, phone, password_hash) VALUES (?, ?, ?, ?)').run(name, email, phone || null, hash);
    const user = { id: result.lastInsertRowid, name, email };
    const token = generateToken(user);
    res.status(201).json({ success: true, data: { user, token } });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.post('/login', (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ success: false, error: 'Email and password required' });

    const db = getDb();
    const user = db.prepare('SELECT * FROM users WHERE email = ?').get(email);
    if (!user || !bcrypt.compareSync(password, user.password_hash)) {
      return res.status(401).json({ success: false, error: 'Invalid credentials' });
    }
    const token = generateToken(user);
    res.json({ success: true, data: { user: { id: user.id, name: user.name, email: user.email, phone: user.phone }, token } });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.get('/profile', verifyToken, (req, res) => {
  try {
    const db = getDb();
    const user = db.prepare('SELECT id, name, email, phone, created_at FROM users WHERE id = ?').get(req.user.id);
    if (!user) return res.status(404).json({ success: false, error: 'User not found' });
    res.json({ success: true, data: user });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
