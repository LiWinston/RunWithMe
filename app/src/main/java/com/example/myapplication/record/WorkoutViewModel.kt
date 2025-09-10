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

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData
    private val _time = MutableLiveData("00:00:00")
    private val _speed = MutableLiveData("0.00 mph")
    private val _distance = MutableLiveData("0.00 miles")
    private val _calories = MutableLiveData("0 kcal")
    private val _debugInfo = MutableLiveData("Idle")
    
    // 新增数据
    private val _steps = MutableLiveData(0)
    private val _heartRate = MutableLiveData(0)
    private val _currentWorkoutId = MutableLiveData<Long?>(null)

    val time: LiveData<String> = _time
    val speed: LiveData<String> = _speed
    val distance: LiveData<String> = _distance
    val calories: LiveData<String> = _calories
    val debugInfo: LiveData<String> = _debugInfo
    val steps: LiveData<Int> = _steps
    val heartRate: LiveData<Int> = _heartRate
    val currentWorkoutId: LiveData<Long?> = _currentWorkoutId

    // 状态
    private var running = false
    private var totalDistance = 0.0
    private var lastLocation: Location? = null
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    // 传感器
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = null

    // 计时
    private var startTime: Long = 0L
    private var pauseOffset: Long = 0L

    // 用户参数
    private val userWeightKg = 65.0
    private val metValue = 8.0
    private val stepLength = 0.75  // 平均步幅（米）

    // 步数
    private var stepCount = 0

    // GPS 控制
    private var lastGpsUpdateTime: Long = 0L
    private var useGps = true
    
    // 路线追踪 - 按距离取样
    private val routePoints = mutableListOf<WorkoutRoute>()
    private var routeSequence = 0
    private var lastRouteLocation: Location? = null
    private val minDistanceForRoute = 10.0 // 最小10米间隔记录路线点
    
    // 改进的传感器数据
    private var lastAcceleration = 0.0
    private var accelerationHistory = mutableListOf<Double>()
    private var simulatedDistance = 0.0 // 模拟器用距离
    private var isSimulatorMode = false // 检测是否是模拟器环境

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
        
        // 检测是否是模拟器环境
        isSimulatorMode = android.os.Build.FINGERPRINT.contains("generic") || 
                         android.os.Build.MODEL.contains("Emulator") ||
                         android.os.Build.MODEL.contains("Android SDK")

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

        // 改进的卡路里计算 - 基于距离和时间
        val hoursFloat = seconds / 3600.0
        val distanceBasedCalories = (totalDistance / 1000) * userWeightKg * 0.8 // 每公里每公斤0.8卡路里
        val timeBasedCalories = metValue * userWeightKg * hoursFloat
        val cal = maxOf(distanceBasedCalories, timeBasedCalories)
        _calories.value = String.format("%.0f kcal", cal)

        // 模拟器模式 - 生成测试数据
        if (isSimulatorMode && seconds > 0) {
            simulateMovement(seconds)
        }
        
        // GPS模式或传感器模式
        if (now - lastGpsUpdateTime > 3000 || isSimulatorMode) {
            useGps = false
            
            // 使用步数或模拟数据计算距离
            val sensorDistance = if (isSimulatorMode) simulatedDistance else stepCount * stepLength
            if (totalDistance < sensorDistance) {
                totalDistance = sensorDistance
                _distance.value = String.format("%.2f miles", totalDistance / 1609.34)
            }
            
            // 计算速度
            if (seconds > 0) {
                val speedMps = totalDistance / seconds
                _speed.value = String.format("%.2f mph", speedMps * 2.23694)
            }
            
            _debugInfo.value = if (isSimulatorMode) "Simulator Mode" else "Accelerometer Mode"
            
            // 在模拟器模式下生成路线点
            if (isSimulatorMode) {
                generateSimulatedRoutePoint()
            }
        }
    }
    
    // 模拟运动数据（模拟器环境下使用）
    private fun simulateMovement(seconds: Int) {
        // 模拟跑步：平均速度 6-8 km/h
        val baseSpeed = 6.5 + Math.sin(seconds * 0.1) * 1.5 // 6.5±1.5 km/h
        val speedMps = baseSpeed / 3.6 // 转换为m/s
        
        // 每秒增加距离
        simulatedDistance += speedMps
        
        // 模拟步数：大约每分钟160步
        val targetSteps = (seconds * 160.0 / 60.0).toInt()
        if (stepCount < targetSteps) {
            stepCount = targetSteps
            _steps.value = stepCount
        }
        
        // 模拟心率：130-160 bpm
        val heartRate = (130 + Math.sin(seconds * 0.05) * 15).toInt()
        _heartRate.value = heartRate
    }
    
    // 生成模拟的路线点
    private fun generateSimulatedRoutePoint() {
        val seconds = ((System.currentTimeMillis() - startTime + pauseOffset) / 1000).toInt()
        
        // 应该按距离间隔生成，而不是时间间隔
        if (totalDistance - (lastRouteLocation?.let { totalDistance } ?: 0.0) >= minDistanceForRoute) {
            // 模拟北京附近的移动路线
            val baseLat = 39.9042 + (routeSequence * 0.0001) // 每个点北移
            val baseLng = 116.4074 + (routeSequence * 0.0001) // 每个点东移
            
            val routePoint = WorkoutRoute(
                latitude = baseLat,
                longitude = baseLng,
                altitude = 50.0 + Math.sin(routeSequence * 0.1) * 5, // 模拟轻微海拔变化
                accuracy = 5.0,
                speed = (totalDistance / maxOf(seconds, 1)) * 3.6, // km/h
                heartRate = _heartRate.value?.takeIf { it > 0 },
                timestamp = java.time.Instant.now().toString(),
                sequenceOrder = ++routeSequence
            )
            
            routePoints.add(routePoint)
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

                        val timeDiff = (location.time - lastLocation!!.time) / 1000.0
                        if (timeDiff > 0) {
                            val speedMps = distance / timeDiff
                            val speedMph = speedMps * 2.23694
                            _speed.value = String.format("%.2f mph", speedMph)
                        }
                    }
                    lastLocation = location
                    lastGpsUpdateTime = System.currentTimeMillis()
                    useGps = true
                    _debugInfo.value = "GPS Mode"
                    
                    // 记录路线点
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
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(accelListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    /** Accelerometer 监听（简易步伐检测） */
    private val accelListener = object : SensorEventListener {
        private var lastUpdate = 0L

        override fun onSensorChanged(event: SensorEvent) {
            if (!running) return
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val accel = Math.sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
            val now = System.currentTimeMillis()

            // ✅ 阈值调低，方便 Emulator 触发
            if (accel > 1.2 && now - lastUpdate > 300) {
                stepCount++
                _steps.value = stepCount
                lastUpdate = now
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
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
        sensorManager.unregisterListener(accelListener)
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

        startTime = 0L
        pauseOffset = 0L
        totalDistance = 0.0
        lastLocation = null
        _time.value = "00:00:00"
        _speed.value = "0.00 mph"
        _distance.value = "0.00 miles"
        _calories.value = "0 kcal"
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
            val routePoint = WorkoutRoute(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = if (location.hasAltitude()) location.altitude else null,
                accuracy = if (location.hasAccuracy()) location.accuracy.toDouble() else null,
                speed = if (location.hasSpeed()) (location.speed * 3.6) else null, // m/s转km/h
                heartRate = _heartRate.value?.takeIf { it > 0 },
                timestamp = java.time.Instant.ofEpochMilli(location.time).toString(),
                sequenceOrder = ++routeSequence
            )
            
            routePoints.add(routePoint)
            lastRouteLocation = location
        }
    }
    
    // 获取当前运动的路线数据
    fun getRoutePoints(): List<WorkoutRoute> {
        return routePoints.toList()
    }
    
    // 清除路线数据
    fun clearRoutePoints() {
        routePoints.clear()
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
        
        return WorkoutCreateRequest(
            userId = 1L, // TODO: 从用户会话获取真实用户ID
            workoutType = "OUTDOOR_RUN",
            distance = totalDistance / 1000, // 米转公里
            duration = durationSeconds,
            steps = stepCount,
            calories = calculateCalories(durationSeconds),
            avgSpeed = calculateAvgSpeed(),
            avgPace = calculateAvgPace(),
            avgHeartRate = _heartRate.value?.takeIf { it > 0 },
            maxHeartRate = _heartRate.value?.takeIf { it > 0 }, // 简化处理，实际应记录最大值
            startTime = java.time.Instant.ofEpochMilli(startTime).toString(),
            endTime = java.time.Instant.now().toString(),
            status = "COMPLETED",
            visibility = "PRIVATE",
            goalAchieved = checkGoalAchievement(totalDistance / 1000, durationSeconds),
            notes = null,
            weatherCondition = generateWeatherCondition(), // 模拟天气条件
            temperature = generateTemperature(), // 模拟温度
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
