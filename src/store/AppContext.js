import React, { createContext, useContext, useReducer } from 'react';

const AppContext = createContext();

const initialState = {
  user: {
    id: 'user-001',
    name: 'Hiker',
    email: 'hiker@example.com',
    phone: '+91-9876543210',
    emergencyContacts: [
      { id: 'ec-1', name: 'Forest Ranger Office', phone: '+91-1800-123-456', relation: 'Authority' },
      { id: 'ec-2', name: 'Emergency Contact', phone: '+91-9876543211', relation: 'Family' },
    ],
    preferences: {
      checkInInterval: 60,
      locationShareEnabled: true,
      fallDetectionEnabled: true,
      silentSOSEnabled: true,
      deviationAlertDistance: 100,
      gpsAccuracy: 'high',
    },
  },
  currentHike: {
    active: false,
    trailId: null,
    trailName: '',
    startTime: null,
    route: [],
    distance: 0,
    duration: 0,
    elevationGain: 0,
    checkIns: [],
    paused: false,
  },
  safetyStatus: {
    lastCheckIn: null,
    nextCheckInDue: null,
    sosActive: false,
    sosTimestamp: null,
    fallDetected: false,
    isSharing: false,
    deviationDetected: false,
    deviationDistance: 0,
  },
  hazards: [],
  activities: [],
  offlineMode: false,
  networkStatus: {
    connected: true,
    signalStrength: 'good',
    type: 'cellular',
  },
  dangerZoneAlerts: [],
  riskLevel: 'low',
  weather: {
    temperature: 24,
    conditions: 'Partly Cloudy',
    humidity: 68,
    windSpeed: 12,
    alerts: [],
  },
};

function appReducer(state, action) {
  switch (action.type) {
    case 'START_HIKE':
      return {
        ...state,
        currentHike: {
          ...state.currentHike,
          active: true,
          trailId: action.payload.trailId,
          trailName: action.payload.trailName,
          startTime: new Date().toISOString(),
          route: [],
          distance: 0,
          duration: 0,
          elevationGain: 0,
          checkIns: [],
          paused: false,
        },
        safetyStatus: {
          ...state.safetyStatus,
          isSharing: state.user.preferences.locationShareEnabled,
        },
      };
    case 'END_HIKE':
      return {
        ...state,
        currentHike: { ...initialState.currentHike },
        safetyStatus: { ...initialState.safetyStatus },
      };
    case 'PAUSE_HIKE':
      return { ...state, currentHike: { ...state.currentHike, paused: true } };
    case 'RESUME_HIKE':
      return { ...state, currentHike: { ...state.currentHike, paused: false } };
    case 'UPDATE_LOCATION':
      return {
        ...state,
        currentHike: {
          ...state.currentHike,
          route: [...state.currentHike.route, action.payload],
          distance: action.payload.totalDistance || state.currentHike.distance,
          elevationGain: action.payload.totalElevation || state.currentHike.elevationGain,
        },
      };
    case 'CHECK_IN':
      return {
        ...state,
        safetyStatus: {
          ...state.safetyStatus,
          lastCheckIn: new Date().toISOString(),
        },
        currentHike: {
          ...state.currentHike,
          checkIns: [...state.currentHike.checkIns, { timestamp: new Date().toISOString(), status: 'safe' }],
        },
      };
    case 'TRIGGER_SOS':
      return {
        ...state,
        safetyStatus: {
          ...state.safetyStatus,
          sosActive: true,
          sosTimestamp: new Date().toISOString(),
        },
      };
    case 'CANCEL_SOS':
      return {
        ...state,
        safetyStatus: { ...state.safetyStatus, sosActive: false, sosTimestamp: null },
      };
    case 'FALL_DETECTED':
      return {
        ...state,
        safetyStatus: { ...state.safetyStatus, fallDetected: true },
      };
    case 'FALL_DISMISSED':
      return {
        ...state,
        safetyStatus: { ...state.safetyStatus, fallDetected: false },
      };
    case 'ADD_HAZARD':
      return {
        ...state,
        hazards: [action.payload, ...state.hazards],
      };
    case 'VALIDATE_HAZARD':
      return {
        ...state,
        hazards: state.hazards.map((h) =>
          h.id === action.payload.id ? { ...h, confirmations: (h.confirmations || 0) + 1 } : h
        ),
      };
    case 'UPDATE_NETWORK':
      return { ...state, networkStatus: { ...state.networkStatus, ...action.payload } };
    case 'SET_OFFLINE':
      return { ...state, offlineMode: action.payload };
    case 'UPDATE_RISK':
      return { ...state, riskLevel: action.payload };
    case 'ADD_ACTIVITY':
      return { ...state, activities: [action.payload, ...state.activities] };
    case 'UPDATE_SETTINGS':
      return {
        ...state,
        user: {
          ...state.user,
          preferences: { ...state.user.preferences, ...action.payload },
        },
      };
    case 'UPDATE_EMERGENCY_CONTACTS':
      return {
        ...state,
        user: { ...state.user, emergencyContacts: action.payload },
      };
    case 'UPDATE_WEATHER':
      return { ...state, weather: { ...state.weather, ...action.payload } };
    case 'SET_DANGER_ALERTS':
      return { ...state, dangerZoneAlerts: action.payload };
    case 'SET_DEVIATION':
      return {
        ...state,
        safetyStatus: {
          ...state.safetyStatus,
          deviationDetected: action.payload.detected,
          deviationDistance: action.payload.distance || 0,
        },
      };
    default:
      return state;
  }
}

export function AppProvider({ children }) {
  const [state, dispatch] = useReducer(appReducer, initialState);
  return (
    <AppContext.Provider value={{ state, dispatch }}>
      {children}
    </AppContext.Provider>
  );
}

export function useAppContext() {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useAppContext must be used within AppProvider');
  }
  return context;
}

export default AppContext;
