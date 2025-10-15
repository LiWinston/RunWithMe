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

    // 新增数据
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

    // 获取步频
    fun getCadence(): Int = cadence

    // 获取传感器状态信息
    fun getSensorInfo(): String {
        val hasAccel = accelerometer != null
        val hasGyro = gyroscope != null
        val hasStepDetector = stepDetector != null
        return "传感器: 加速度计($hasAccel) 陀螺仪($hasGyro) 步数检测器($hasStepDetector)"
    }

    // 根据速度判断运动类型
    // 参考研究:
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

    // 状态
    private var running = false
    private var totalDistance = 0.0
    private var lastLocation: Location? = null
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    // 传感器
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var stepDetector: Sensor? = null

    // 计时
    private var startTime: Long = 0L
    private var pauseOffset: Long = 0L

    // 用户参数 hardcoding
    private val userWeightKg = 65.0
    private val metValue = 8.0
    private val stepLength = 0.75  // 平均步幅（米）

    // 步数
    private var stepCount = 0

    // GPS 控制
    private var lastGpsUpdateTime: Long = 0L
    private var useGps = true

    // 动态数据采样 - 用于JSON存储
    private val routePoints = mutableListOf<RoutePoint>()
    private val speedSamples = mutableListOf<SpeedSample>()
    private val heartRateSamples = mutableListOf<HeartRateSample>()
    private val elevationSamples = mutableListOf<ElevationSample>()
    private val paceSamples = mutableListOf<PaceSample>()
    private val cadenceSamples = mutableListOf<CadenceSample>()
    private val accuracySamples = mutableListOf<AccuracySample>()

    private var routeSequence = 0
    private var lastRouteLocation: Location? = null
    private var lastRouteDistance = 0.0 // 上次记录路线点时的距离
    private val minDistanceForRoute = 2.0 // 最小10米间隔记录路线点

    // 改进的传感器数据
    private var lastAcceleration = 0.0
    private var accelerationHistory = mutableListOf<Double>()
    private var simulatedDistance = 0.0 // 模拟器用距离
    private var isSimulatorMode = false // 检测是否是模拟器环境

    // 步频分析
    private var stepTimestamps = mutableListOf<Long>()
    private var cadence = 0 // 步频 (步/分钟)

    // 陀螺仪数据
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

        // 重置路线追踪相关变量
        clearWorkoutData()

        // 重置传感器数据
        stepTimestamps.clear()
        accelerationHistory.clear()
        cadence = 0

        // 不再检测模拟器环境，允许在所有设备上正常工作
        isSimulatorMode = false

        startLocationTracking()
        startStepSensors()

        if (isSimulatorMode) {
            _debugInfo.value = "Simulator Mode - Generating test data"
        }
    }

    /** 每秒调用一次 */
    fun tick() {
        if (!running) return

        val now = System.currentTimeMillis()
        val elapsedMillis = (now - startTime) + pauseOffset
        val seconds = (elapsedMillis / 1000).toInt()

        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        _time.value = String.format("%02d:%02d:%02d", hours, minutes, secs)

        // 卡路里计算
        val hoursFloat = seconds / 3600.0
        val distanceBasedCalories = (totalDistance / 1000) * userWeightKg * 0.8
        val timeBasedCalories = metValue * userWeightKg * hoursFloat
        val cal = maxOf(distanceBasedCalories, timeBasedCalories)
        _calories.value = String.format("%.0f kcal", cal)

        // 心率模拟（无心率传感器时）
        if (_heartRate.value == null || _heartRate.value == 0) {
            val simulatedHeartRate = (120 + Math.sin(seconds * 0.05) * 20).toInt()
            _heartRate.value = simulatedHeartRate
        }

        // 每5秒记录一次动态数据（用于图表，不是UI实时显示）
        if (seconds > 0 && seconds % 5 == 0) {
            val heartRate = _heartRate.value ?: 0
            val latestSpeed = _speed.value?.replace(" m/s", "")?.toFloatOrNull() ?: 0f
            recordDynamicData(latestSpeed, heartRate, seconds)
        }

    }

    /** 启动 GPS 定位 */
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
                        val distance = lastLocation!!.distanceTo(location) // 米

                        // =============================
                        // 🚫 跳点过滤逻辑
                        // =============================
                        if (distance > 50f) {
                            _debugInfo.value =
                                "GPS jump ignored (${String.format("%.1f", distance)} m, no line)"
                            // ✅ 不计入距离、不画线，但仍更新位置
                            lastLocation = location
                            lastGpsUpdateTime = System.currentTimeMillis()
                            useGps = true
                            continue // 跳过本次后续逻辑
                        }

                        // =============================
                        // ✅ 正常点：更新距离和速度
                        // =============================
                        totalDistance += distance
                        _distance.value = String.format("%.2f m", totalDistance)

                        val timeDiff = (location.time - lastLocation!!.time) / 1000.0 // 秒
                        if (timeDiff > 0) {
                            val speedMps = distance / timeDiff
                            _speed.value = if (speedMps < 0.5) "0.00 m/s" else String.format("%.2f m/s", speedMps)
                            _workoutType.value = determineWorkoutType(speedMps)
                        }

                        // ✅ 仅正常点才画线
                        recordRoutePoint(location)
                    } else {
                        _speed.value = "0.00 m/s"
                    }

                    //总是更新 lastLocation（包括跳点）
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

    /** 启动加速度传感器 */
    private fun startStepSensors() {
        // 加速度计
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        accelerometer?.let {
//            sensorManager.registerListener(accelListener, it, SensorManager.SENSOR_DELAY_GAME)
//        }

        // 陀螺仪
//        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
//        gyroscope?.let {
//            sensorManager.registerListener(gyroListener, it, SensorManager.SENSOR_DELAY_GAME)
//        }

        // 步数检测器
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
//            // 添加到加速度历史
//            accelerationHistory.add(accel)
//            if (accelerationHistory.size > 50) {
//                accelerationHistory.removeAt(0)
//            }
//
//            // 改进的步数检测算法（峰值检测）
//            if (accelerationHistory.size >= 3 && now - lastUpdate > 250) {
//                val current = accelerationHistory[accelerationHistory.size - 1]
//                val previous = accelerationHistory[accelerationHistory.size - 2]
//                val beforePrevious = accelerationHistory[accelerationHistory.size - 3]
//
//                // 寻找局部峰值
//                if (previous > current && previous > beforePrevious && previous > 2.0) {
//                    stepCount++
//                    _steps.value = stepCount
//                    lastUpdate = now
//
//                    // 记录步数时间戳用于步频计算
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

    /** 陀螺仪监听器 */
//    private val gyroListener = object : SensorEventListener {
//        override fun onSensorChanged(event: SensorEvent) {
//            if (!running) return
//            rotationRateX = event.values[0].toDouble()
//            rotationRateY = event.values[1].toDouble()
//            rotationRateZ = event.values[2].toDouble()
//
//            // 陀螺仪数据可用于检测跑步姿态和稳定性
//            val totalRotation = Math.sqrt(rotationRateX * rotationRateX +
//                                        rotationRateY * rotationRateY +
//                                        rotationRateZ * rotationRateZ)
//
//            // 基于运动状态调整步长
//            if (totalRotation > 1.0) {
//                // 不稳定运动，可能在快速跑步
//                // 可以调整步长或其他参数，目前没有用到
//            }
//        }
//
//        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//    }

    /** 硬件步数检测器（更准确） */
    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (running && stepDetector != null) {
                // 使用硬件步数检测器，更准确
                stepCount++
                _steps.value = stepCount

                val now = System.currentTimeMillis()
                stepTimestamps.add(now)
                calculateCadence()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /** 计算步频 */
    private fun calculateCadence() {
        val now = System.currentTimeMillis()
        // 保留最近1分钟的步数时间戳
        stepTimestamps.removeAll { it < now - 60000 }

        // 计算步频（步/分钟）
        cadence = stepTimestamps.size
    }

    // 控制函数
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

        // 卸载所有传感器监听器
//        sensorManager.unregisterListener(accelListener)
//        sensorManager.unregisterListener(gyroListener)
        sensorManager.unregisterListener(stepListener)

        // 停止GPS追踪
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

        // 重置所有状态
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

    // 记录路线点 - 按距离取样而非时间取样
    private fun recordRoutePoint(location: Location) {
        if (!running) return

        // 检查距离间隔：只有移动了足够距离才记录新的路线点
        val shouldRecord = lastRouteLocation?.let { lastLoc ->
            val distance = lastLoc.distanceTo(location)
            distance >= minDistanceForRoute
        } ?: true // 第一个点总是记录

        if (shouldRecord) {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

            val routePoint = RoutePoint(
                lat = location.latitude,
                lng = location.longitude,
                altitude = if (location.hasAltitude()) location.altitude else null,
                timestamp = sdf.format(Date(location.time)), // 兼容 API 24
                sequence = ++routeSequence
            )

            routePoints.add(routePoint)
            lastRouteLocation = location
            lastRouteDistance = totalDistance // 同步更新距离记录

            // 记录GPS精度

            if (location.hasAccuracy()) {
                accuracySamples.add(
                    AccuracySample(
                        accuracy = location.accuracy.toDouble(),
                        timestamp = sdf.format(Date(location.time)) // 兼容 API 24
                    )
                )
            }


            if (location.hasAltitude()) {
                elevationSamples.add(
                    ElevationSample(
                        elevation = location.altitude,
                        timestamp = sdf.format(Date(location.time)) // 兼容 API 24
                    )
                )
            }

            // 调试信息
            _debugInfo.value = "GPS Mode - ${routePoints.size} route points"
        }
    }

    // 记录动态数据（定期采样）
    private fun recordDynamicData(currentSpeed: Float, currentHeartRate: Int, elapsedSeconds: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date())

        // 记录速度
        speedSamples.add(SpeedSample(
            speed = currentSpeed,
            timestamp = timestamp
        ))

        // 记录心率
        if (currentHeartRate > 0) {
            heartRateSamples.add(HeartRateSample(
                heartRate = currentHeartRate,
                timestamp = timestamp
            ))
        }

        // 记录配速
        if (currentSpeed > 0) {
            val pace = (3600 / currentSpeed).toInt() // 秒/公里
            paceSamples.add(PaceSample(
                pace = pace,
                timestamp = timestamp
            ))
        }

        // 记录步频
        if (cadence > 0) {
            cadenceSamples.add(CadenceSample(
                cadence = cadence,
                timestamp = timestamp
            ))
        }
    }

    // 获取完整的运动动态数据
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

    // 获取当前运动的路线数据（兼容性）
    fun getRoutePoints(): List<RoutePoint> {
        return routePoints.toList()
    }

    // 清除所有动态数据
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
        return metValue * userWeightKg * hours
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
            (durationSeconds / distanceM).toInt() // 秒/米
        } else null
    }

    private fun checkGoalAchievement(distanceM: Double, durationSeconds: Int): Boolean {
        return distanceM >= 1000 || durationSeconds >= 900 // 1km或15分钟
    }
}

// 动态数据类定义 - 对应后端JSON结构
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

// 完整的运动动态数据结构
data class WorkoutDynamicData(
    val route: List<RoutePoint>,
    val speedSamples: List<SpeedSample>,
    val heartRateSamples: List<HeartRateSample>,
    val elevationSamples: List<ElevationSample>,
    val paceSamples: List<PaceSample>,
    val cadenceSamples: List<CadenceSample>,
    val locationAccuracy: List<AccuracySample>
)
