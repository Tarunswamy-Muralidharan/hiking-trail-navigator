package com.hikingtrailnavigator.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object TrailList : Screen("trails")
    data object TrailDetail : Screen("trail/{trailId}") {
        fun createRoute(trailId: String) = "trail/$trailId"
    }
    data object Navigate : Screen("navigate")
    data object Directions : Screen("directions/{trailId}") {
        fun createRoute(trailId: String) = "directions/$trailId"
    }
    data object ActiveHike : Screen("active_hike/{trailId}") {
        fun createRoute(trailId: String) = "active_hike/$trailId"
    }
    data object ActivitySummary : Screen("activity_summary/{activityId}") {
        fun createRoute(activityId: String) = "activity_summary/$activityId"
    }
    data object ActivityHistory : Screen("activity_history")
    data object Safety : Screen("safety")
    data object SOS : Screen("sos")
    data object HazardReport : Screen("hazard_report")
    data object LiveTracking : Screen("live_tracking")
    data object EmergencyContacts : Screen("emergency_contacts")
    data object RouteWarnings : Screen("route_warnings")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object AdminLogin : Screen("admin_login")
    data object AdminDashboard : Screen("admin_dashboard")
    data object AdminManageTrails : Screen("admin_manage_trails")
    data object AdminAddTrail : Screen("admin_add_trail")
}

enum class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    Home("home", Icons.Default.Home, "Home"),
    Trails("trails", Icons.Default.Terrain, "Trails"),
    Navigate("navigate", Icons.Default.Navigation, "Navigate"),
    Safety("safety", Icons.Default.Shield, "Safety"),
    Profile("profile", Icons.Default.Person, "Profile")
}
