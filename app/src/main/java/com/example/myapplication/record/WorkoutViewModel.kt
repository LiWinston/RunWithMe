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
import java.time.Instant
import java.util.Date
import java.util.Locale

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData
    private val _time = MutableLiveData("00:00:00")
    private val _speed = MutableLiveData("0.00 mph")
    private val _distance = MutableLiveData("0.00 miles")
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
    private val minDistanceForRoute = 10.0 // 最小10米间隔记录路线点

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
            val latestSpeed = _speed.value?.replace(" mph", "")?.toFloatOrNull() ?: 0f
            recordDynamicData(latestSpeed * 1.60934, heartRate, seconds) // mph -> km/h
        }

        // 如果没GPS，用传感器生成路线
        if (!useGps) {
            generateSensorBasedRoutePoint()
        }
    }


    // 模拟运动数据（模拟器环境下使用）
    private fun simulateMovement(seconds: Int) {
        // 模拟跑步：平均速度 8-12 km/h（适中的跑步速度）
        val baseSpeed = 10.0 + Math.sin(seconds * 0.05) * 2.0 // 10±2 km/h
        val speedMps = baseSpeed / 3.6 // 转换为m/s

        // 每秒增加距离（但要控制总距离合理）
        val previousDistance = simulatedDistance
        simulatedDistance += speedMps

        // 模拟步数：大约每分钟180步（正常跑步步频）
        val targetSteps = (seconds * 180.0 / 60.0).toInt()
        if (stepCount < targetSteps) {
            stepCount = targetSteps
            _steps.value = stepCount

            // 更新步频
            val now = System.currentTimeMillis()
            stepTimestamps.add(now)
            calculateCadence()
        }

        // 模拟心率：140-170 bpm（跑步心率）
        val heartRate = (140 + Math.sin(seconds * 0.03) * 15).toInt()
        _heartRate.value = heartRate

        // 采样动态数据（每5秒采样一次）
        if (seconds % 5 == 0) {
            recordDynamicData(speedMps * 3.6, heartRate, seconds)
        }

        // 调试信息包含更多详情
        _debugInfo.value = "Simulator Mode - ${routePoints.size} points, ${cadence} bpm"
    }

    // 生成模拟的路线点
    private fun generateSimulatedRoutePoint() {
        val seconds = ((System.currentTimeMillis() - startTime + pauseOffset) / 1000).toInt()

        // 修复bug：应该按距离间隔生成，而不是时间间隔
        if (totalDistance - lastRouteDistance >= minDistanceForRoute) {
            // 模拟北京附近的移动路线
            val baseLat = 39.9042 + (routeSequence * 0.0001) // 每个点北移
            val baseLng = 116.4074 + (routeSequence * 0.0001) // 每个点东移

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val timestamp = sdf.format(java.util.Date())

            val routePoint = RoutePoint(
                lat = baseLat,
                lng = baseLng,
                altitude = 50.0 + Math.sin(routeSequence * 0.1) * 5, // 模拟轻微海拔变化
                timestamp = timestamp,
                sequence = ++routeSequence
            )

            routePoints.add(routePoint)
            lastRouteDistance = totalDistance // 更新上次记录的距离

            // 调试信息
            _debugInfo.value = "Simulator Mode - ${routePoints.size} route points"
        }
    }

    // 传感器模式下生成基于距离的模拟路线点
    private fun generateSensorBasedRoutePoint() {
        if (totalDistance - lastRouteDistance >= minDistanceForRoute) {
            // 在传感器模式下，基于步数生成模拟路线
            val baseLat = 39.9042 + (routeSequence * 0.0001) // 每个点北移
            val baseLng = 116.4074 + (routeSequence * 0.0001) // 每个点东移

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val timestamp = sdf.format(Date())

            val routePoint = RoutePoint(
                lat = baseLat,
                lng = baseLng,
                altitude = 50.0 + Math.sin(routeSequence * 0.1) * 3, // 较小的海拔变化
                timestamp = timestamp,
                sequence = ++routeSequence
            )

            routePoints.add(routePoint)
            lastRouteDistance = totalDistance // 更新上次记录的距离

            // 调试信息
            _debugInfo.value = "Accelerometer Mode - ${routePoints.size} route points"
        }
    }

    /** 启动 GPS 定位 */
    fun startLocationTracking() {
        val context = getApplication<Application>()

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000
        ).setMinUpdateIntervalMillis(1000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (!running) return
                for (location in result.locations) {
                    if (lastLocation != null) {
                        val distance = lastLocation!!.distanceTo(location) // 米
                        totalDistance += distance
                        _distance.value = String.format("%.2f miles", totalDistance / 1609.34)

                        // 用距离差和时间差计算速度，避免跳值
                        val timeDiff = (location.time - lastLocation!!.time) / 1000.0 // 秒
                        if (timeDiff > 0) {
                            val speedMps = distance / timeDiff  // 米/秒
                            val speedMph = speedMps * 2.23694   // 转 mph

                            // 平滑处理：速度<0.5当作静止
                            _speed.value = if (speedMph < 0.5) {
                                "0.00 mph"
                            } else {
                                String.format("%.2f mph", speedMph)
                            }

                            // 更新运动类型
                            _workoutType.value = determineWorkoutType(speedMps)
                        }
                    } else {
                        // 第一次定位没有 lastLocation
                        _speed.value = "0.00 mph"
                    }

                    // 更新状态（只保留一次）
                    lastLocation = location
                    lastGpsUpdateTime = System.currentTimeMillis()
                    useGps = true
                    _debugInfo.value = "GPS Mode"

                    recordRoutePoint(location)
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
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(accelListener, it, SensorManager.SENSOR_DELAY_GAME)
        }

        // 陀螺仪
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        gyroscope?.let {
            sensorManager.registerListener(gyroListener, it, SensorManager.SENSOR_DELAY_GAME)
        }

        // 步数检测器（如果设备支持）
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        stepDetector?.let {
            sensorManager.registerListener(stepListener, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    /** Accelerometer 监听（改进的步伐检测） */
    private val accelListener = object : SensorEventListener {
        private var lastUpdate = 0L
        private var lastZ = 0.0

        override fun onSensorChanged(event: SensorEvent) {
            if (!running) return
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble())
            val accel = magnitude - SensorManager.GRAVITY_EARTH
            val now = System.currentTimeMillis()

            // 添加到加速度历史
            accelerationHistory.add(accel)
            if (accelerationHistory.size > 50) {
                accelerationHistory.removeAt(0)
            }

            // 改进的步数检测算法（峰值检测）
            if (accelerationHistory.size >= 3 && now - lastUpdate > 250) {
                val current = accelerationHistory[accelerationHistory.size - 1]
                val previous = accelerationHistory[accelerationHistory.size - 2]
                val beforePrevious = accelerationHistory[accelerationHistory.size - 3]

                // 寻找局部峰值
                if (previous > current && previous > beforePrevious && previous > 2.0) {
                    stepCount++
                    _steps.value = stepCount
                    lastUpdate = now

                    // 记录步数时间戳用于步频计算
                    stepTimestamps.add(now)
                    calculateCadence()
                }
            }

            lastAcceleration = accel
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /** 陀螺仪监听器 */
    private val gyroListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!running) return
            rotationRateX = event.values[0].toDouble()
            rotationRateY = event.values[1].toDouble()
            rotationRateZ = event.values[2].toDouble()

            // 陀螺仪数据可用于检测跑步姿态和稳定性
            val totalRotation = Math.sqrt(rotationRateX * rotationRateX +
                                        rotationRateY * rotationRateY +
                                        rotationRateZ * rotationRateZ)

            // 基于运动状态调整步长
            if (totalRotation > 1.0) {
                // 不稳定运动，可能在快速跑步
                // 可以调整步长或其他参数
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

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
        sensorManager.unregisterListener(accelListener)
        sensorManager.unregisterListener(gyroListener)
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
        _speed.value = "0.00 mph"
        _distance.value = "0.00 miles"
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
    private fun recordDynamicData(currentSpeed: Double, currentHeartRate: Int, elapsedSeconds: Int) {
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

    // 获取运动数据用于保存
    fun getWorkoutData(): WorkoutCreateRequest {
        val durationSeconds = if (startTime > 0) {
            ((System.currentTimeMillis() - startTime + pauseOffset) / 1000).toInt()
        } else 0

        // 获取当前位置（用于天气查询）
        val currentLat = if (isSimulatorMode) 39.9042 else lastLocation?.latitude
        val currentLng = if (isSimulatorMode) 116.4074 else lastLocation?.longitude

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        val startTimeStr = sdf.format(Date(startTime))   // startTime 是 Long
        val endTimeStr = sdf.format(Date())              // 当前时间

        return WorkoutCreateRequest(
            userId = 1L,
            workoutType = "OUTDOOR_RUN",
            distance = totalDistance / 1000.0,
            duration = durationSeconds,
            steps = stepCount,
            calories = calculateCalories(durationSeconds),
            avgSpeed = calculateAvgSpeed(),
            avgPace = calculateAvgPace(),
            avgHeartRate = _heartRate.value?.takeIf { it > 0 },
            maxHeartRate = _heartRate.value?.takeIf { it > 0 },
            startTime = startTimeStr,
            endTime = endTimeStr,
            status = "COMPLETED",
            visibility = "PRIVATE",
            goalAchieved = checkGoalAchievement(totalDistance / 1000, durationSeconds),
            notes = null,
            weatherCondition = generateWeatherCondition(),
            temperature = generateTemperature(),
            latitude = currentLat,
            longitude = currentLng
        )
    }

    // 生成模拟天气条件
    private fun generateWeatherCondition(): String {
        val conditions = listOf("晴天", "多云", "阴天", "小雨", "薄雾")
        return conditions.random()
    }

    // 生成模拟温度
    private fun generateTemperature(): Double {
        // 模拟20-30度的温度
        return 20.0 + Math.random() * 10.0
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
            (totalDistance / 1000) / (durationSeconds / 3600.0) // km/h
        } else null
    }

    private fun calculateAvgPace(): Int? {
        val distanceKm = totalDistance / 1000
        val durationSeconds = if (startTime > 0) {
            ((System.currentTimeMillis() - startTime + pauseOffset) / 1000).toInt()
        } else 0

        return if (distanceKm > 0) {
            (durationSeconds / distanceKm).toInt() // 秒/公里
        } else null
    }

    private fun checkGoalAchievement(distanceKm: Double, durationSeconds: Int): Boolean {
        return distanceKm >= 1.0 || durationSeconds >= 900 // 1km或15分钟
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
    val speed: Double,
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
