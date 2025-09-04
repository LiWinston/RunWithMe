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

    val time: LiveData<String> = _time
    val speed: LiveData<String> = _speed
    val distance: LiveData<String> = _distance
    val calories: LiveData<String> = _calories
    val debugInfo: LiveData<String> = _debugInfo

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

        startLocationTracking()
        startStepSensors()
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

        val hoursFloat = seconds / 3600.0
        val cal = metValue * userWeightKg * hoursFloat
        _calories.value = String.format("%.0f kcal", cal)

        // ✅ 如果 GPS 超过 3 秒没更新 → fallback 到传感器
        if (now - lastGpsUpdateTime > 3000) {
            useGps = false
            val estDistance = stepCount * stepLength
            if (totalDistance < estDistance) {
                totalDistance = estDistance
                _distance.value = String.format("%.2f miles", totalDistance / 1609.34)
            }
            if (seconds > 0 && stepCount > 0) {
                val speedMps = (stepCount * stepLength) / seconds
                _speed.value = String.format("%.2f mph", speedMps * 2.23694)
            }
            _debugInfo.value = "Accelerometer Mode"
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
}
