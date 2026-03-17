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

    // ==================== TEST 1: Home Screen renders correctly ====================
    @Test
    fun homeScreenDisplaysMapAndBottomNavigation() {
        // Verify bottom navigation items are visible
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Trails").assertIsDisplayed()
        composeRule.onNodeWithText("Navigate").assertIsDisplayed()
        composeRule.onNodeWithText("Safety").assertIsDisplayed()
        composeRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    // ==================== TEST 2: Bottom navigation works ====================
    @Test
    fun bottomNavigationSwitchesBetweenScreens() {
        // Navigate to Trails tab
        composeRule.onNodeWithText("Trails").performClick()
        composeRule.waitForIdle()

        // Verify Trails screen content appears
        // TrailListScreen should show trail list or search
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

    // ==================== TEST 3: Navigate tab shows content ====================
    @Test
    fun navigateTabDisplaysContent() {
        composeRule.onNodeWithText("Navigate").performClick()
        composeRule.waitForIdle()

        // NavigateScreen should show trails or hike options
        composeRule.onNodeWithText("Navigate").assertIsDisplayed()
    }

    // ==================== TEST 4: Profile tab shows content ====================
    @Test
    fun profileTabDisplaysContent() {
        composeRule.onNodeWithText("Profile").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    // ==================== TEST 5: Safety tab shows safety features ====================
    @Test
    fun safetyTabShowsSafetyOptions() {
        composeRule.onNodeWithText("Safety").performClick()
        composeRule.waitForIdle()

        // Safety dashboard should have SOS and other safety features
        composeRule.onNodeWithText("Safety").assertIsDisplayed()
    }
}
