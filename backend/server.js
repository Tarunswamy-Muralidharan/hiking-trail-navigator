require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const { initDatabase } = require('./config/database');

const authRoutes = require('./routes/auth');
const trailRoutes = require('./routes/trails');
const safetyRoutes = require('./routes/safety');
const hazardRoutes = require('./routes/hazards');
const activityRoutes = require('./routes/activities');
const contactRoutes = require('./routes/contacts');
const dangerZoneRoutes = require('./routes/dangerZones');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(helmet());
app.use(cors());
app.use(morgan('dev'));
app.use(express.json({ limit: '10mb' }));

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/trails', trailRoutes);
app.use('/api/safety', safetyRoutes);
app.use('/api/hazards', hazardRoutes);
app.use('/api/activities', activityRoutes);
app.use('/api/contacts', contactRoutes);
app.use('/api/danger-zones', dangerZoneRoutes);

// Health check
app.get('/api/health', (req, res) => {
  res.json({ success: true, message: 'Hiking Trail Navigator API is running', timestamp: new Date().toISOString() });
});

// Error handling
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ success: false, error: 'Internal server error', message: err.message });
});

// 404
app.use((req, res) => {
  res.status(404).json({ success: false, error: 'Route not found' });
});

// Initialize database (async) then start server
async function start() {
  await initDatabase();
  app.listen(PORT, () => {
    console.log('Hiking Trail Navigator API running on port ' + PORT);
  });
}

start().catch((err) => {
  console.error('Failed to start server:', err);
  process.exit(1);
});

module.exports = app;
