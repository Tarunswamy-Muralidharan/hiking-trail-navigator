const mockWeatherData = {
  temperature: 24,
  feelsLike: 26,
  humidity: 68,
  windSpeed: 12,
  windDirection: 'NW',
  conditions: 'Partly Cloudy',
  icon: 'partly-sunny',
  rainProbability: 30,
  visibility: 8,
  uvIndex: 6,
};

const mockAlerts = [
  {
    id: 'wa-001', type: 'rain', severity: 'warning',
    message: 'Heavy rainfall expected in the afternoon. Trails may become slippery.',
    validFrom: '2026-03-12T12:00:00', validTo: '2026-03-12T18:00:00',
  },
];

const mockForecast = [
  { hour: 0, temp: 20, conditions: 'Clear', rain: 0 },
  { hour: 3, temp: 19, conditions: 'Clear', rain: 0 },
  { hour: 6, temp: 18, conditions: 'Foggy', rain: 5 },
  { hour: 9, temp: 22, conditions: 'Partly Cloudy', rain: 10 },
  { hour: 12, temp: 26, conditions: 'Cloudy', rain: 40 },
  { hour: 15, temp: 24, conditions: 'Rain', rain: 70 },
  { hour: 18, temp: 22, conditions: 'Light Rain', rain: 50 },
  { hour: 21, temp: 20, conditions: 'Partly Cloudy', rain: 15 },
];

export const getCurrentWeather = async (latitude, longitude) => {
  await new Promise((r) => setTimeout(r, 300));
  return { ...mockWeatherData, latitude, longitude };
};

export const getWeatherAlerts = async (latitude, longitude) => {
  await new Promise((r) => setTimeout(r, 200));
  return mockAlerts;
};

export const assessWeatherRisk = (weatherData) => {
  if (weatherData.rainProbability > 70 || weatherData.windSpeed > 40) return 'severe';
  if (weatherData.rainProbability > 50 || weatherData.windSpeed > 25) return 'high';
  if (weatherData.rainProbability > 30 || weatherData.windSpeed > 15) return 'medium';
  return 'low';
};

export const getWeatherForecast = async (latitude, longitude, hours = 24) => {
  await new Promise((r) => setTimeout(r, 300));
  return mockForecast;
};

export default { getCurrentWeather, getWeatherAlerts, assessWeatherRisk, getWeatherForecast };
