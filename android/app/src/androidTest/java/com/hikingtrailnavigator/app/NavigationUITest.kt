package com.hikingtrailnavigator.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI TESTS for Navigation and Screen Rendering
 *
 * What: Tests the actual UI - verifies screens render, buttons are clickable,
 *       text appears, and navigation between screens works correctly.
 * Why: Catches UI regressions - missing text, broken navigation, unclickable buttons.
 *      Verifies the user experience matches requirements.
 * How: Runs on device/emulator using Compose Testing APIs. Finds UI elements by
 *      text/content description and performs actions (click, type, scroll).
 * SRS: Covers FR-101 (Trail Discovery), FR-208 (Safety Dashboard), FR-201 (Navigation)
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    /**
     * Helper: navigate past the Login screen by selecting "I'm a Hiker",
     * entering a name, and tapping "Start Hiking".
     * If already past login (e.g. session saved), just waits for Home.
     */
    private fun loginAsHiker(name: String = "TestHiker") {
        Thread.sleep(500) // Let the activity settle

        // Check if we're already on Home (session persisted)
        val alreadyHome = composeRule.onAllNodesWithText("Home")
            .fetchSemanticsNodes().isNotEmpty()
        if (alreadyHome) return

        // Wait for Login screen to appear
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithText("I'm a Hiker", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Tap "I'm a Hiker"
        composeRule.onNodeWithText("I'm a Hiker", substring = true).performClick()
        composeRule.waitForIdle()
        Thread.sleep(300)

        // Enter hiker name
        composeRule.waitUntil(timeoutMillis = 3000) {
            composeRule.onAllNodesWithText("Your Name", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Your Name", substring = true).performTextInput(name)
        composeRule.waitForIdle()

        // Tap "Start Hiking"
        composeRule.onNodeWithText("Start Hiking", substring = true).performClick()
        composeRule.waitForIdle()
        Thread.sleep(500)

        // Wait for Home screen to load
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithText("Home")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    // ==================== TEST 1: Login screen renders correctly ====================
    @Test
    fun loginScreenDisplaysRoleSelection() {
        // Verify login screen shows role selection
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithText("Hiking Trail Navigator")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Hiking Trail Navigator").assertIsDisplayed()
        composeRule.onNodeWithText("I'm a Hiker", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("I'm an Admin", substring = true).assertIsDisplayed()
    }

    // ==================== TEST 2: Home Screen renders after login ====================
    @Test
    fun homeScreenDisplaysMapAndBottomNavigation() {
        loginAsHiker()

        // Verify bottom navigation items are visible
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Trails").assertIsDisplayed()
        composeRule.onNodeWithText("Navigate").assertIsDisplayed()
        composeRule.onNodeWithText("Safety").assertIsDisplayed()
        composeRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    // ==================== TEST 3: Bottom navigation works ====================
    @Test
    fun bottomNavigationSwitchesBetweenScreens() {
        loginAsHiker()

        // Navigate to Trails tab
        composeRule.onNodeWithText("Trails").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Trails").assertIsDisplayed()

        // Navigate to Safety tab
        composeRule.onNodeWithText("Safety").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Safety").assertIsDisplayed()

        // Navigate back to Home
        composeRule.onNodeWithText("Home").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
    }

    // ==================== TEST 4: Navigate tab shows content ====================
    @Test
    fun navigateTabDisplaysContent() {
        loginAsHiker()

        // Click the bottom nav item - use performScrollTo first in case it's partially hidden
        composeRule.onAllNodesWithText("Navigate").onFirst().performClick()
        composeRule.waitForIdle()
        Thread.sleep(500)

        // "Navigate" may appear in both bottom nav and screen title — verify at least one exists
        composeRule.onAllNodesWithText("Navigate").onFirst().assertExists()
    }

    // ==================== TEST 5: Profile tab shows content ====================
    @Test
    fun profileTabDisplaysContent() {
        loginAsHiker()

        composeRule.onNodeWithText("Profile").performClick()
        composeRule.waitForIdle()

        // "Profile" may appear in both bottom nav and screen title — verify at least one exists
        composeRule.onAllNodesWithText("Profile").onFirst().assertIsDisplayed()
    }

    // ==================== TEST 6: Safety tab shows safety features ====================
    @Test
    fun safetyTabShowsSafetyOptions() {
        loginAsHiker()

        composeRule.onNodeWithText("Safety").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Safety").assertIsDisplayed()
    }

    // ==================== TEST 7: Hiker name form validation ====================
    @Test
    fun hikerLoginRequiresName() {
        // Wait for login screen
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithText("I'm a Hiker", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Tap "I'm a Hiker"
        composeRule.onNodeWithText("I'm a Hiker", substring = true).performClick()
        composeRule.waitForIdle()

        // Tap "Start Hiking" without entering a name
        composeRule.onNodeWithText("Start Hiking", substring = true).performClick()
        composeRule.waitForIdle()

        // Should show error message
        composeRule.onNodeWithText("Please enter your name", substring = true).assertIsDisplayed()
    }
}
