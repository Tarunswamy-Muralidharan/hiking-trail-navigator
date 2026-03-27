package com.hikingtrailnavigator.app.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Fall detection using accelerometer + gyroscope.
 * 3-phase algorithm:
 *   1. Freefall phase: acceleration drops below ~3 m/s² (near weightlessness)
 *   2. Impact phase: sudden spike above ~25 m/s²
 *   3. Orientation change: gyroscope detects significant rotation after impact
 *
 * All 3 phases must occur within a time window to trigger a fall alert,
 * reducing false positives from normal hiking activities (jumping, sitting down, etc.)
 */
@Singleton
class FallDetectionService @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _fallDetected = MutableStateFlow(false)
    val fallDetected: StateFlow<Boolean> = _fallDetected.asStateFlow()

    private var isMonitoring = false

    // Thresholds (slightly raised - still demo-friendly but less false positives)
    private val impactThreshold = 18.0f      // m/s² (real=25, prev demo=15)
    private val freefallThreshold = 4.5f     // m/s² (real=3, prev demo=5)
    private val rotationThreshold = 2.5f     // rad/s (real=5, prev demo=2)
    private val fallWindowMs = 2500L         // ms (real=2000, prev demo=3000)
    private val cooldownMs = 5000L           // minimum time between fall alerts

    // State tracking for 3-phase detection
    private var freefallDetectedTime = 0L
    private var impactDetectedTime = 0L
    private var rotationDetectedTime = 0L
    private var lastFallAlertTime = 0L

    // Rolling window for acceleration to detect freefall-to-impact pattern
    private var prevAccelMagnitude = SensorManager.GRAVITY_EARTH

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopMonitoring() {
        isMonitoring = false
        sensorManager.unregisterListener(this)
        resetState()
    }

    fun resetFallDetection() {
        _fallDetected.value = false
        resetState()
    }

    private fun resetState() {
        freefallDetectedTime = 0L
        impactDetectedTime = 0L
        rotationDetectedTime = 0L
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val now = System.currentTimeMillis()

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z)

                // Phase 1: Freefall detection (near zero-G)
                if (magnitude < freefallThreshold) {
                    freefallDetectedTime = now
                }

                // Phase 2: Impact detection (high-G spike)
                // Must come after freefall for it to be a real fall
                if (magnitude > impactThreshold) {
                    impactDetectedTime = now
                }

                prevAccelMagnitude = magnitude
            }

            Sensor.TYPE_GYROSCOPE -> {
                val gx = event.values[0]
                val gy = event.values[1]
                val gz = event.values[2]
                val rotationMagnitude = sqrt(gx * gx + gy * gy + gz * gz)

                // Phase 3: Significant rotation (body tumbling/falling over)
                if (rotationMagnitude > rotationThreshold) {
                    rotationDetectedTime = now
                }
            }
        }

        // Check if all 3 phases occurred within the time window
        checkFallPattern(now)
    }

    private fun checkFallPattern(now: Long) {
        // Cooldown check
        if (now - lastFallAlertTime < cooldownMs) return

        // Need at least freefall + impact within window
        if (freefallDetectedTime == 0L || impactDetectedTime == 0L) return

        val freefallAge = now - freefallDetectedTime
        val impactAge = now - impactDetectedTime

        // Both must be recent (within window)
        if (freefallAge > fallWindowMs || impactAge > fallWindowMs) return

        // Freefall must come before or very close to impact
        if (impactDetectedTime < freefallDetectedTime - 200) return

        // If we have a gyroscope, require rotation too (stronger detection)
        // If no gyroscope available, fall back to 2-phase (accel only)
        if (gyroscope != null) {
            if (rotationDetectedTime == 0L) return
            val rotationAge = now - rotationDetectedTime
            if (rotationAge > fallWindowMs) return
        }

        // All phases confirmed - fall detected
        _fallDetected.value = true
        lastFallAlertTime = now
        resetState()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
