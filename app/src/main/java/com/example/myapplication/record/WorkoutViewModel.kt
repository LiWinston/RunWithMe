package com.example.myapplication.record

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData
    private val _time = MutableLiveData("00:00:00")
    private val _speed = MutableLiveData("0.00 m/s")
    private val _distance = MutableLiveData("0.00 m")
    private val _calories = MutableLiveData("0 kcal")
    private val _debugInfo = MutableLiveData("Idle")
    private val _workoutType = MutableLiveData("Running")

    // Additional data
    private val _steps = MutableLiveData(0)
    private val _heartRate = MutableLiveData(0)
    private val _currentWorkoutId = MutableLiveData<Long?>(null)

    val time: LiveData<String> = _time
    val speed: LiveData<String> = _speed
    val distance: LiveData<String> = _distance
    val calories: LiveData<String> = _calories
    val debugInfo: LiveData<String> = _debugInfo
    val workoutType: LiveData<String> = _workoutType
    val steps: LiveData<Int> = _steps
    val heartRate: LiveData<Int> = _heartRate
    val currentWorkoutId: LiveData<Long?> = _currentWorkoutId

    // Get cadence
    fun getCadence(): Int = cadence

    // Get sensor status information
    fun getSensorInfo(): String {
        val hasAccel = accelerometer != null
        val hasGyro = gyroscope != null
        val hasStepDetector = stepDetector != null
        return "Sensors: Accelerometer($hasAccel) Gyroscope($hasGyro) StepDetector($hasStepDetector)"
    }

    // Determine workout type based on speed
    // Reference research:
    // Walking: ~90 m/min (1.5 m/s), Brisk Walking: ~112 m/min (1.87 m/s)
    // Jogging: >134 m/min (2.23 m/s), Running: 2.5-4.5 m/s
    private fun determineWorkoutType(speedMps: Double): String {
        return when {
            speedMps < 1.5 -> "Walking"           // < 1.5 m/s (~5.4 km/h)
            speedMps < 2.23 -> "Brisk Walking"    // 1.5-2.23 m/s (~5.4-8.0 km/h)
            speedMps < 2.5 -> "Jogging"           // 2.23-2.5 m/s (~8.0-9.0 km/h)
            speedMps < 3.5 -> "Running"           // 2.5-3.5 m/s (~9.0-12.6 km/h)
            else -> "Fast Running"                // > 3.5 m/s (>12.6 km/h)
        }
    }

    // State
    private var running = false
    private var totalDistance = 0.0
    private var lastLocation: Location? = null
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    // Sensors
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var stepDetector: Sensor? = null

    // Timing
    private var startTime: Long = 0L
    private var pauseOffset: Long = 0L

    // User parameters (hardcoded)
    private val userWeightKg = 65.0
    private val stepLength = 0.75  // Average step length (meters)
    
    // MET values for different workout types (based on ACSM standards)
    private fun getMetValue(workoutType: String): Double {
        return when (workoutType) {
            "Walking" -> 3.5           // Slow walking
            "Brisk Walking" -> 4.5     // Brisk walking
            "Jogging" -> 7.0           // Jogging
            "Running" -> 9.8           // Running
            "Fast Running" -> 12.3     // Fast running
            else -> 8.0                // Default
        }
    }

    // Step count
    private var stepCount = 0

    // GPS control
    private var lastGpsUpdateTime: Long = 0L
    private var useGps = true

    // Dynamic data sampling - for JSON storage
    private val routePoints = mutableListOf<RoutePoint>()
    private val speedSamples = mutableListOf<SpeedSample>()
    private val heartRateSamples = mutableListOf<HeartRateSample>()
    private val elevationSamples = mutableListOf<ElevationSample>()
    private val paceSamples = mutableListOf<PaceSample>()
    private val cadenceSamples = mutableListOf<CadenceSample>()
    private val accuracySamples = mutableListOf<AccuracySample>()

    private var routeSequence = 0
    private var lastRouteLocation: Location? = null
    private var lastRouteDistance = 0.0 // Last recorded route point distance
    private val minDistanceForRoute = 2.0 // Minimum 2m interval for recording route points

    // Improved sensor data
    private var lastAcceleration = 0.0
    private var accelerationHistory = mutableListOf<Double>()
    private var simulatedDistance = 0.0 // Distance for simulator
    private var isSimulatorMode = false // Detect if running in simulator

    // Cadence analysis
    private var stepTimestamps = mutableListOf<Long>()
    private var cadence = 0 // Cadence (steps/minute)

    // Gyroscope data
    private var rotationRateX = 0.0
    private var rotationRateY = 0.0
    private var rotationRateZ = 0.0

    private lateinit var locationCallback: LocationCallback

    /** 开始锻炼 */
    fun startWorkout() {
        running = true
        startTime = System.currentTimeMillis()
        pauseOffset = 0L
        totalDistance = 0.0
        lastLocation = null
        stepCount = 0
        lastGpsUpdateTime = 0L
        useGps = true
        simulatedDistance = 0.0

        // Reset route tracking variables
        clearWorkoutData()

        // Reset sensor data
        stepTimestamps.clear()
        accelerationHistory.clear()
        cadence = 0

        // No longer detect simulator environment, allow normal operation on all devices
        isSimulatorMode = false

        startLocationTracking()
        startStepSensors()

        if (isSimulatorMode) {
            _debugInfo.value = "Simulator Mode - Generating test data"
        }
    }

    /** Called every second */
    fun tick() {
        if (!running) return

        val now = System.currentTimeMillis()
        val elapsedMillis = (now - startTime) + pauseOffset
        val seconds = (elapsedMillis / 1000).toInt()

        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        _time.value = String.format("%02d:%02d:%02d", hours, minutes, secs)

        // Calorie calculation - using scientifically proven formulas
        val hoursFloat = seconds / 3600.0
        val distanceKm = totalDistance / 1000.0
        val currentWorkoutType = _workoutType.value ?: "Running"
        
        // Method 1: Distance-based formula (most accurate for running)
        // For walking: 0.57 × weight × distance (km)
        // For running: 1.036 × weight × distance (km)
        val avgSpeed = if (seconds > 0) totalDistance / seconds else 0.0
        val distanceBasedCalories = if (avgSpeed < 2.23) {
            // Walking formula
            0.57 * userWeightKg * distanceKm
        } else {
            // Running formula (widely used in exercise physiology)
            userWeightKg * distanceKm * 1.036
        }
        
        // Method 2: MET-based formula (time-based, adjusted by workout type)
        val metValue = getMetValue(currentWorkoutType)
        val timeBasedCalories = metValue * userWeightKg * hoursFloat
        
        // Use distance-based as primary, time-based as fallback for minimal movement
        val cal = if (distanceKm > 0.01) {
            distanceBasedCalories
        } else {
            timeBasedCalories
        }
        
        _calories.value = String.format("%.0f kcal", cal)

        // Heart rate simulation (when no heart rate sensor available)
        if (_heartRate.value == null || _heartRate.value == 0) {
            val simulatedHeartRate = (120 + Math.sin(seconds * 0.05) * 20).toInt()
            _heartRate.value = simulatedHeartRate
        }

        // Record dynamic data every 5 seconds (for charts, not real-time UI display)
        if (seconds > 0 && seconds % 5 == 0) {
            val heartRate = _heartRate.value ?: 0
            val latestSpeed = _speed.value?.replace(" m/s", "")?.toFloatOrNull() ?: 0f
            recordDynamicData(latestSpeed, heartRate, seconds)
        }

    }

    /** Start GPS tracking */
    fun startLocationTracking() {
        val context = getApplication<Application>()

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 200
        ).setMinUpdateIntervalMillis(200).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (!running) return
                for (location in result.locations) {
                    if (lastLocation != null) {
                        val distance = lastLocation!!.distanceTo(location) // meters

                        // =============================
                        // GPS jump filtering logic
                        // =============================
                        if (distance > 50f) {
                            _debugInfo.value =
                                "GPS jump ignored (${String.format("%.1f", distance)} m, no line)"
                            // Update position but don't count distance or draw line
                            lastLocation = location
                            lastGpsUpdateTime = System.currentTimeMillis()
                            useGps = true
                            continue // Skip subsequent logic for this iteration
                        }

                        // =============================
                        // Normal point: update distance and speed
                        // =============================
                        totalDistance += distance
                        _distance.value = String.format("%.2f m", totalDistance)

                        val timeDiff = (location.time - lastLocation!!.time) / 1000.0 // seconds
                        if (timeDiff > 0) {
                            val speedMps = distance / timeDiff
                            _speed.value = if (speedMps < 0.5) "0.00 m/s" else String.format("%.2f m/s", speedMps)
                            _workoutType.value = determineWorkoutType(speedMps)
                        }

                        // Draw line only for normal points
                        recordRoutePoint(location)
                    } else {
                        _speed.value = "0.00 m/s"
                    }

                    // Always update lastLocation (including jump points)
                    lastLocation = location
                    lastGpsUpdateTime = System.currentTimeMillis()
                    useGps = true
                    _debugInfo.value = "GPS Mode"
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )
        } else {
            _speed.value = "No GPS permission"
        }
    }

    /** Start accelerometer sensor */
    private fun startStepSensors() {
        // Accelerometer
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        accelerometer?.let {
//            sensorManager.registerListener(accelListener, it, SensorManager.SENSOR_DELAY_GAME)
//        }

        // Gyroscope
//        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
//        gyroscope?.let {
//            sensorManager.registerListener(gyroListener, it, SensorManager.SENSOR_DELAY_GAME)
//        }

        // Step detector
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        stepDetector?.let {
            sensorManager.registerListener(stepListener, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

//    /** Accelerometer 监听（改进的步伐检测） */
//    private val accelListener = object : SensorEventListener {
//        private var lastUpdate = 0L
//        private var lastZ = 0.0
//
//        override fun onSensorChanged(event: SensorEvent) {
//            if (!running) return
//            if (stepDetector != null) return
//            val x = event.values[0]
//            val y = event.values[1]
//            val z = event.values[2]
//
//            val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble())
//            val accel = magnitude - SensorManager.GRAVITY_EARTH
//            val now = System.currentTimeMillis()
//
//            // Add to acceleration history
//            accelerationHistory.add(accel)
//            if (accelerationHistory.size > 50) {
//                accelerationHistory.removeAt(0)
//            }
//
//            // Improved step detection algorithm (peak detection)
//            if (accelerationHistory.size >= 3 && now - lastUpdate > 250) {
//                val current = accelerationHistory[accelerationHistory.size - 1]
//                val previous = accelerationHistory[accelerationHistory.size - 2]
//                val beforePrevious = accelerationHistory[accelerationHistory.size - 3]
//
//                // Find local peak
//                if (previous > current && previous > beforePrevious && previous > 2.0) {
//                    stepCount++
//                    _steps.value = stepCount
//                    lastUpdate = now
//
//                    // Record step timestamp for cadence calculation
//                    stepTimestamps.add(now)
//                    calculateCadence()
//                }
//            }
//
//            lastAcceleration = accel
//        }
//
//        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//    }

    /** Gyroscope listener */
//    private val gyroListener = object : SensorEventListener {
//        override fun onSensorChanged(event: SensorEvent) {
//            if (!running) return
//            rotationRateX = event.values[0].toDouble()
//            rotationRateY = event.values[1].toDouble()
//            rotationRateZ = event.values[2].toDouble()
//
//            // Gyroscope data can be used to detect running posture and stability
//            val totalRotation = Math.sqrt(rotationRateX * rotationRateX +
//                                        rotationRateY * rotationRateY +
//                                        rotationRateZ * rotationRateZ)
//
//            // Adjust step length based on motion state
//            if (totalRotation > 1.0) {
//                // Unstable motion, possibly fast running
//                // Can adjust step length or other parameters, currently not used
//            }
//        }
//
//        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//    }

    /** Hardware step detector (more accurate) */
    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (running && stepDetector != null) {
                // Use hardware step detector for better accuracy
                stepCount++
                _steps.value = stepCount

                val now = System.currentTimeMillis()
                stepTimestamps.add(now)
                calculateCadence()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /** Calculate cadence */
    private fun calculateCadence() {
        val now = System.currentTimeMillis()
        // Keep only timestamps from the last 1 minute
        stepTimestamps.removeAll { it < now - 60000 }

        // Calculate cadence (steps/minute)
        cadence = stepTimestamps.size
    }

    // Control functions
    fun pauseWorkout() {
        if (running) {
            running = false
            pauseOffset += System.currentTimeMillis() - startTime
        }
    }

    fun resumeWorkout() {
        if (!running) {
            running = true
            startTime = System.currentTimeMillis()
        }
    }

    fun stopWorkout() {
        running = false
        stepCount = 0

        // Unregister all sensor listeners
//        sensorManager.unregisterListener(accelListener)
//        sensorManager.unregisterListener(gyroListener)
        sensorManager.unregisterListener(stepListener)

        // Stop GPS tracking
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

        // Reset all states
        startTime = 0L
        pauseOffset = 0L
        totalDistance = 0.0
        lastLocation = null
        stepTimestamps.clear()
        accelerationHistory.clear()
        cadence = 0

        _time.value = "00:00:00"
        _speed.value = "0.00 m/s"
        _distance.value = "0.00 m"
        _calories.value = "0 kcal"
        _steps.value = 0
        _heartRate.value = 0
        _debugInfo.value = "Stopped"
    }

    // Record route point - sample by distance rather than time
    private fun recordRoutePoint(location: Location) {
        if (!running) return

        // Check distance interval: only record new route point if moved enough distance
        val shouldRecord = lastRouteLocation?.let { lastLoc ->
            val distance = lastLoc.distanceTo(location)
            distance >= minDistanceForRoute
        } ?: true // Always record the first point

        if (shouldRecord) {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

            val routePoint = RoutePoint(
                lat = location.latitude,
                lng = location.longitude,
                altitude = if (location.hasAltitude()) location.altitude else null,
                timestamp = sdf.format(Date(location.time)), // Compatible with API 24
                sequence = ++routeSequence
            )

            routePoints.add(routePoint)
            lastRouteLocation = location
            lastRouteDistance = totalDistance // Sync update distance record

            // Record GPS accuracy

            if (location.hasAccuracy()) {
                accuracySamples.add(
                    AccuracySample(
                        accuracy = location.accuracy.toDouble(),
                        timestamp = sdf.format(Date(location.time)) // Compatible with API 24
                    )
                )
            }


            if (location.hasAltitude()) {
                elevationSamples.add(
                    ElevationSample(
                        elevation = location.altitude,
                        timestamp = sdf.format(Date(location.time)) // Compatible with API 24
                    )
                )
            }

            // Debug info
            _debugInfo.value = "GPS Mode - ${routePoints.size} route points"
        }
    }

    // Record dynamic data (periodic sampling)
    private fun recordDynamicData(currentSpeed: Float, currentHeartRate: Int, elapsedSeconds: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date())

        // Record speed
        speedSamples.add(SpeedSample(
            speed = currentSpeed,
            timestamp = timestamp
        ))

        // Record heart rate
        if (currentHeartRate > 0) {
            heartRateSamples.add(HeartRateSample(
                heartRate = currentHeartRate,
                timestamp = timestamp
            ))
        }

        // Record pace
        if (currentSpeed > 0) {
            val pace = (3600 / currentSpeed).toInt() // seconds/kilometer
            paceSamples.add(PaceSample(
                pace = pace,
                timestamp = timestamp
            ))
        }

        // Record cadence
        if (cadence > 0) {
            cadenceSamples.add(CadenceSample(
                cadence = cadence,
                timestamp = timestamp
            ))
        }
    }

    // Get complete workout dynamic data
    fun getWorkoutDynamicData(): WorkoutDynamicData {
        return WorkoutDynamicData(
            route = routePoints.toList(),
            speedSamples = speedSamples.toList(),
            heartRateSamples = heartRateSamples.toList(),
            elevationSamples = elevationSamples.toList(),
            paceSamples = paceSamples.toList(),
            cadenceSamples = cadenceSamples.toList(),
            locationAccuracy = accuracySamples.toList()
        )
    }

    // Get current workout route data (compatibility)
    fun getRoutePoints(): List<RoutePoint> {
        return routePoints.toList()
    }

    // Clear all dynamic data
    fun clearWorkoutData() {
        routePoints.clear()
        speedSamples.clear()
        heartRateSamples.clear()
        elevationSamples.clear()
        paceSamples.clear()
        cadenceSamples.clear()
        accuracySamples.clear()
        routeSequence = 0
    }

    private fun calculateCalories(durationSeconds: Int): Double? {
        val hours = durationSeconds / 3600.0
        val distanceKm = totalDistance / 1000.0
        val currentWorkoutType = _workoutType.value ?: "Running"
        
        // Use distance-based formula if we have distance data
        val avgSpeed = if (durationSeconds > 0) totalDistance / durationSeconds else 0.0
        return if (distanceKm > 0.01) {
            if (avgSpeed < 2.23) {
                // Walking formula
                0.57 * userWeightKg * distanceKm
            } else {
                // Running formula
                userWeightKg * distanceKm * 1.036
            }
        } else {
            // Fallback to MET-based formula
            val metValue = getMetValue(currentWorkoutType)
            metValue * userWeightKg * hours
        }
    }

    private fun calculateAvgSpeed(): Double? {
        val durationSeconds = if (startTime > 0) {
            ((System.currentTimeMillis() - startTime + pauseOffset) / 1000).toInt()
        } else 0

        return if (durationSeconds > 0 && totalDistance > 0) {
            totalDistance / durationSeconds // m/s
        } else null
    }

    private fun calculateAvgPace(): Int? {
        val distanceM = totalDistance
        val durationSeconds = if (startTime > 0) {
            ((System.currentTimeMillis() - startTime + pauseOffset) / 1000).toInt()
        } else 0

        return if (distanceM > 0) {
            (durationSeconds / distanceM).toInt() // seconds/meter
        } else null
    }

    private fun checkGoalAchievement(distanceM: Double, durationSeconds: Int): Boolean {
        return distanceM >= 1000 || durationSeconds >= 900 // 1km or 15 minutes
    }
}

// Dynamic data class definitions - corresponding to backend JSON structure
data class RoutePoint(
    val lat: Double,
    val lng: Double,
    val altitude: Double?,
    val timestamp: String,
    val sequence: Int
)

data class SpeedSample(
    val speed: Float,
    val timestamp: String
)

data class HeartRateSample(
    val heartRate: Int,
    val timestamp: String
)

data class ElevationSample(
    val elevation: Double,
    val timestamp: String
)

data class PaceSample(
    val pace: Int,
    val timestamp: String
)

data class CadenceSample(
    val cadence: Int,
    val timestamp: String
)

data class AccuracySample(
    val accuracy: Double,
    val timestamp: String
)

// Complete workout dynamic data structure
data class WorkoutDynamicData(
    val route: List<RoutePoint>,
    val speedSamples: List<SpeedSample>,
    val heartRateSamples: List<HeartRateSample>,
    val elevationSamples: List<ElevationSample>,
    val paceSamples: List<PaceSample>,
    val cadenceSamples: List<CadenceSample>,
    val locationAccuracy: List<AccuracySample>
)
