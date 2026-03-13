const initSqlJs = require('sql.js');
const fs = require('fs');
const path = require('path');

const DB_PATH = process.env.DB_PATH || path.join(__dirname, '..', 'hiking_navigator.db');
let db = null;
let SQL = null;

async function initSQL() {
  if (!SQL) {
    SQL = await initSqlJs();
  }
  return SQL;
}

function getDb() {
  if (!db) {
    throw new Error('Database not initialized. Call initDatabase() first.');
  }
  return db;
}

// Wrapper to mimic better-sqlite3 API
function wrapDb(sqliteDb) {
  const wrapper = {
    _db: sqliteDb,
    exec(sql) {
      sqliteDb.run(sql);
    },
    prepare(sql) {
      return {
        run(...params) {
          sqliteDb.run(sql, params);
          // Get last insert rowid
          const result = sqliteDb.exec('SELECT last_insert_rowid() as id');
          const lastId = result.length > 0 ? result[0].values[0][0] : 0;
          // Get changes
          const changes = sqliteDb.getRowsModified();
          return { lastInsertRowid: lastId, changes };
        },
        get(...params) {
          const stmt = sqliteDb.prepare(sql);
          stmt.bind(params);
          if (stmt.step()) {
            const cols = stmt.getColumnNames();
            const vals = stmt.get();
            stmt.free();
            const row = {};
            cols.forEach((c, i) => { row[c] = vals[i]; });
            return row;
          }
          stmt.free();
          return undefined;
        },
        all(...params) {
          const results = [];
          const stmt = sqliteDb.prepare(sql);
          stmt.bind(params);
          while (stmt.step()) {
            const cols = stmt.getColumnNames();
            const vals = stmt.get();
            const row = {};
            cols.forEach((c, i) => { row[c] = vals[i]; });
            results.push(row);
          }
          stmt.free();
          return results;
        },
      };
    },
    save() {
      const data = sqliteDb.export();
      const buffer = Buffer.from(data);
      fs.writeFileSync(DB_PATH, buffer);
    },
  };
  return wrapper;
}

async function initDatabase() {
  const SQL = await initSQL();

  // Load existing db or create new
  let sqliteDb;
  if (fs.existsSync(DB_PATH)) {
    const buffer = fs.readFileSync(DB_PATH);
    sqliteDb = new SQL.Database(buffer);
  } else {
    sqliteDb = new SQL.Database();
  }

  db = wrapDb(sqliteDb);

  db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      email TEXT UNIQUE NOT NULL,
      phone TEXT,
      password_hash TEXT NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS emergency_contacts (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      name TEXT NOT NULL,
      phone TEXT NOT NULL,
      relation TEXT DEFAULT 'Other',
      priority INTEGER DEFAULT 0,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id)
    );

    CREATE TABLE IF NOT EXISTS trails (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      description TEXT,
      difficulty TEXT CHECK(difficulty IN ('Easy','Moderate','Hard','Expert')),
      distance_km REAL,
      estimated_duration_min INTEGER,
      elevation_gain_m INTEGER,
      rating REAL DEFAULT 0,
      start_lat REAL,
      start_lng REAL,
      end_lat REAL,
      end_lng REAL,
      region TEXT,
      popularity INTEGER DEFAULT 0,
      coverage_status TEXT DEFAULT 'partial',
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS trail_coordinates (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      trail_id INTEGER NOT NULL,
      sequence INTEGER NOT NULL,
      latitude REAL NOT NULL,
      longitude REAL NOT NULL,
      altitude REAL,
      FOREIGN KEY (trail_id) REFERENCES trails(id)
    );

    CREATE TABLE IF NOT EXISTS activities (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      trail_id INTEGER,
      start_time DATETIME,
      end_time DATETIME,
      distance_km REAL,
      duration_min REAL,
      elevation_gain_m INTEGER,
      avg_pace REAL,
      status TEXT DEFAULT 'completed',
      route_data TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id)
    );

    CREATE TABLE IF NOT EXISTS hazards (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      trail_id INTEGER,
      reporter_id INTEGER,
      type TEXT NOT NULL,
      severity TEXT DEFAULT 'medium',
      description TEXT,
      latitude REAL,
      longitude REAL,
      photo_url TEXT,
      verified INTEGER DEFAULT 0,
      confirmation_count INTEGER DEFAULT 0,
      status TEXT DEFAULT 'active',
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      expires_at DATETIME,
      FOREIGN KEY (trail_id) REFERENCES trails(id),
      FOREIGN KEY (reporter_id) REFERENCES users(id)
    );

    CREATE TABLE IF NOT EXISTS hazard_validations (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      hazard_id INTEGER NOT NULL,
      user_id INTEGER NOT NULL,
      confirmed INTEGER DEFAULT 1,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (hazard_id) REFERENCES hazards(id),
      FOREIGN KEY (user_id) REFERENCES users(id),
      UNIQUE(hazard_id, user_id)
    );

    CREATE TABLE IF NOT EXISTS danger_zones (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      type TEXT,
      description TEXT,
      center_lat REAL NOT NULL,
      center_lng REAL NOT NULL,
      radius_m INTEGER NOT NULL,
      severity TEXT DEFAULT 'medium',
      source TEXT DEFAULT 'community',
      verified INTEGER DEFAULT 0,
      report_count INTEGER DEFAULT 0,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS emergency_alerts (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      type TEXT NOT NULL,
      latitude REAL,
      longitude REAL,
      trail_id INTEGER,
      status TEXT DEFAULT 'active',
      message TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      resolved_at DATETIME,
      FOREIGN KEY (user_id) REFERENCES users(id)
    );

    CREATE TABLE IF NOT EXISTS safety_checkins (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      activity_id INTEGER,
      status TEXT DEFAULT 'safe',
      latitude REAL,
      longitude REAL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id)
    );

    CREATE TABLE IF NOT EXISTS trail_reviews (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      trail_id INTEGER NOT NULL,
      user_id INTEGER NOT NULL,
      rating INTEGER NOT NULL,
      comment TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (trail_id) REFERENCES trails(id),
      FOREIGN KEY (user_id) REFERENCES users(id)
    );
  `);

  // Save to disk
  db.save();
  console.log('Database initialized successfully');
}

module.exports = { getDb, initDatabase };
