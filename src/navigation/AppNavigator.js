import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import { colors } from '../constants/theme';

import HomeScreen from '../screens/HomeScreen';
import TrailDiscoveryScreen from '../screens/TrailDiscoveryScreen';
import TrailDetailScreen from '../screens/TrailDetailScreen';
import NavigationScreen from '../screens/NavigationScreen';
import ActivityScreen from '../screens/ActivityScreen';
import ActivityHistoryScreen from '../screens/ActivityHistoryScreen';
import SafetyDashboardScreen from '../screens/SafetyDashboardScreen';
import SOSScreen from '../screens/SOSScreen';
import HazardReportScreen from '../screens/HazardReportScreen';
import LiveTrackingScreen from '../screens/LiveTrackingScreen';
import EmergencyContactsScreen from '../screens/EmergencyContactsScreen';
import ProfileScreen from '../screens/ProfileScreen';
import SettingsScreen from '../screens/SettingsScreen';

const Tab = createBottomTabNavigator();
const Stack = createNativeStackNavigator();

const screenOptions = {
  headerStyle: { backgroundColor: colors.primary },
  headerTintColor: colors.textWhite,
  headerTitleStyle: { fontWeight: '600' },
};

function HomeStack() {
  return (
    <Stack.Navigator screenOptions={screenOptions}>
      <Stack.Screen name="HomeMain" component={HomeScreen} options={{ title: 'Trail Navigator' }} />
    </Stack.Navigator>
  );
}

function TrailsStack() {
  return (
    <Stack.Navigator screenOptions={screenOptions}>
      <Stack.Screen name="TrailDiscovery" component={TrailDiscoveryScreen} options={{ title: 'Discover Trails' }} />
      <Stack.Screen name="TrailDetail" component={TrailDetailScreen} options={{ title: 'Trail Details' }} />
    </Stack.Navigator>
  );
}

function NavigateStack() {
  return (
    <Stack.Navigator screenOptions={screenOptions}>
      <Stack.Screen name="NavigationMain" component={NavigationScreen} options={{ title: 'Navigate', headerShown: false }} />
      <Stack.Screen name="Activity" component={ActivityScreen} options={{ title: 'Activity Summary' }} />
      <Stack.Screen name="ActivityHistory" component={ActivityHistoryScreen} options={{ title: 'Hike History' }} />
    </Stack.Navigator>
  );
}

function SafetyStack() {
  return (
    <Stack.Navigator screenOptions={screenOptions}>
      <Stack.Screen name="SafetyDashboard" component={SafetyDashboardScreen} options={{ title: 'Safety Center' }} />
      <Stack.Screen name="SOS" component={SOSScreen} options={{ title: 'Emergency SOS', headerStyle: { backgroundColor: colors.danger } }} />
      <Stack.Screen name="HazardReport" component={HazardReportScreen} options={{ title: 'Report Hazard' }} />
      <Stack.Screen name="LiveTracking" component={LiveTrackingScreen} options={{ title: 'Live Tracking' }} />
      <Stack.Screen name="EmergencyContacts" component={EmergencyContactsScreen} options={{ title: 'Emergency Contacts' }} />
    </Stack.Navigator>
  );
}

function ProfileStack() {
  return (
    <Stack.Navigator screenOptions={screenOptions}>
      <Stack.Screen name="ProfileMain" component={ProfileScreen} options={{ title: 'Profile' }} />
      <Stack.Screen name="Settings" component={SettingsScreen} options={{ title: 'Settings' }} />
    </Stack.Navigator>
  );
}

export default function AppNavigator() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarIcon: ({ focused, color, size }) => {
          let iconName;
          switch (route.name) {
            case 'Home': iconName = focused ? 'map' : 'map-outline'; break;
            case 'Trails': iconName = focused ? 'compass' : 'compass-outline'; break;
            case 'Navigate': iconName = focused ? 'navigate' : 'navigate-outline'; break;
            case 'Safety': iconName = focused ? 'shield-checkmark' : 'shield-checkmark-outline'; break;
            case 'Profile': iconName = focused ? 'person' : 'person-outline'; break;
            default: iconName = 'help-circle';
          }
          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.textLight,
        tabBarStyle: {
          backgroundColor: colors.surface,
          borderTopColor: colors.border,
          paddingBottom: 5,
          paddingTop: 5,
          height: 60,
        },
        tabBarLabelStyle: { fontSize: 11, fontWeight: '500' },
      })}
    >
      <Tab.Screen name="Home" component={HomeStack} />
      <Tab.Screen name="Trails" component={TrailsStack} />
      <Tab.Screen name="Navigate" component={NavigateStack} />
      <Tab.Screen name="Safety" component={SafetyStack} />
      <Tab.Screen name="Profile" component={ProfileStack} />
    </Tab.Navigator>
  );
}
