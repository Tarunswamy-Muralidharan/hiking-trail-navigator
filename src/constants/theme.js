export const colors = {
  primary: '#2E7D32',
  primaryLight: '#60AD5E',
  primaryDark: '#005005',
  secondary: '#FF6F00',
  secondaryLight: '#FFA040',
  secondaryDark: '#C43E00',
  danger: '#D32F2F',
  dangerLight: '#FF6659',
  warning: '#F9A825',
  warningLight: '#FFD95A',
  info: '#1565C0',
  infoLight: '#5E92F3',
  success: '#2E7D32',
  background: '#F5F5F5',
  surface: '#FFFFFF',
  text: '#212121',
  textLight: '#757575',
  textWhite: '#FFFFFF',
  border: '#E0E0E0',
  overlay: 'rgba(0,0,0,0.5)',
  transparent: 'transparent',
};

export const typography = {
  h1: { fontSize: 28, fontWeight: '700' },
  h2: { fontSize: 24, fontWeight: '700' },
  h3: { fontSize: 20, fontWeight: '600' },
  h4: { fontSize: 16, fontWeight: '600' },
  body: { fontSize: 14, fontWeight: '400' },
  bodyBold: { fontSize: 14, fontWeight: '600' },
  caption: { fontSize: 12, fontWeight: '400' },
  small: { fontSize: 10, fontWeight: '400' },
};

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

export const borderRadius = {
  sm: 4,
  md: 8,
  lg: 16,
  xl: 24,
  full: 999,
};

export const shadows = {
  small: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  medium: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 4,
    elevation: 4,
  },
  large: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 8,
  },
};

export default { colors, typography, spacing, borderRadius, shadows };
