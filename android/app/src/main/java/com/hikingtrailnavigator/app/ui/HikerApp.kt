package com.hikingtrailnavigator.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hikingtrailnavigator.app.ui.navigation.BottomNavItem
import com.hikingtrailnavigator.app.ui.navigation.Screen
import com.hikingtrailnavigator.app.ui.screens.admin.AdminDashboardScreen
import com.hikingtrailnavigator.app.ui.screens.admin.AdminLoginScreen
import com.hikingtrailnavigator.app.ui.screens.home.HomeScreen
import com.hikingtrailnavigator.app.ui.screens.navigate.ActiveHikeScreen
import com.hikingtrailnavigator.app.ui.screens.navigate.ActivityHistoryScreen
import com.hikingtrailnavigator.app.ui.screens.navigate.NavigateScreen
import com.hikingtrailnavigator.app.ui.screens.profile.ProfileScreen
import com.hikingtrailnavigator.app.ui.screens.profile.SettingsScreen
import com.hikingtrailnavigator.app.ui.screens.safety.*
import com.hikingtrailnavigator.app.ui.screens.trails.TrailDetailScreen
import com.hikingtrailnavigator.app.ui.screens.trails.TrailListScreen
import com.hikingtrailnavigator.app.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on detail/sub screens
    val showBottomBar = currentDestination?.route in BottomNavItem.entries.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    BottomNavItem.entries.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onTrailClick = { trailId ->
                        navController.navigate(Screen.TrailDetail.createRoute(trailId))
                    },
                    onSosClick = {
                        navController.navigate(Screen.SOS.route)
                    }
                )
            }

            composable(Screen.TrailList.route) {
                TrailListScreen(
                    onTrailClick = { trailId ->
                        navController.navigate(Screen.TrailDetail.createRoute(trailId))
                    }
                )
            }

            composable(
                route = Screen.TrailDetail.route,
                arguments = listOf(navArgument("trailId") { type = NavType.StringType })
            ) {
                TrailDetailScreen(
                    onBack = { navController.popBackStack() },
                    onStartHike = { trailId ->
                        navController.navigate(Screen.ActiveHike.createRoute(trailId))
                    }
                )
            }

            composable(Screen.Navigate.route) {
                NavigateScreen(
                    onStartHike = { trailId ->
                        navController.navigate(Screen.ActiveHike.createRoute(trailId))
                    },
                    onViewHistory = {
                        navController.navigate(Screen.ActivityHistory.route)
                    }
                )
            }

            composable(
                route = Screen.ActiveHike.route,
                arguments = listOf(navArgument("trailId") { type = NavType.StringType })
            ) {
                ActiveHikeScreen(
                    onBack = { navController.popBackStack() },
                    onHikeComplete = {
                        // Try Navigate first, fall back to Home if not in back stack
                        val popped = navController.popBackStack(Screen.Navigate.route, false)
                        if (!popped) {
                            navController.popBackStack(Screen.Home.route, false)
                        }
                    }
                )
            }

            composable(Screen.ActivityHistory.route) {
                ActivityHistoryScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Safety.route) {
                SafetyDashboardScreen(
                    onSosClick = { navController.navigate(Screen.SOS.route) },
                    onHazardReportClick = { navController.navigate(Screen.HazardReport.route) },
                    onLiveTrackingClick = { navController.navigate(Screen.LiveTracking.route) },
                    onEmergencyContactsClick = { navController.navigate(Screen.EmergencyContacts.route) }
                )
            }

            composable(Screen.SOS.route) {
                SOSScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.HazardReport.route) {
                HazardReportScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.LiveTracking.route) {
                LiveTrackingScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.EmergencyContacts.route) {
                EmergencyContactsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.RouteWarnings.route) {
                RouteWarningsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onEmergencyContactsClick = { navController.navigate(Screen.EmergencyContacts.route) },
                    onAdminClick = { navController.navigate(Screen.AdminLogin.route) },
                    onRouteWarningsClick = { navController.navigate(Screen.RouteWarnings.route) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }

            // Admin screens
            composable(Screen.AdminLogin.route) {
                AdminLoginScreen(
                    onBack = { navController.popBackStack() },
                    onLoginSuccess = {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.AdminLogin.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
