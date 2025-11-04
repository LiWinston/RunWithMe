package com.example.myapplication.record

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.gms.location.LocationServices
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.Priority

private const val REQ_ACTIVITY_RECOGNITION = 101


class RecordingFragment : Fragment(), OnMapReadyCallback {

    private val workoutViewModel: WorkoutViewModel by activityViewModels()
    private val handler = Handler(Looper.getMainLooper())

    // 地图相关
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private var routePolyline: Polyline? = null
    private val routePoints = mutableListOf<LatLng>()
    private var userMarker: Marker? = null

    // UI组件
    private lateinit var tvTime: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvGpsStatus: TextView
    private lateinit var btnPause: ImageButton
    private lateinit var btnStop: ImageButton

    // 暂停状态
    private var isPaused = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recording, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupMap(savedInstanceState)
        observeWorkoutData()
        setupButtons()
        startTimer()
        requestLocationPermission()
        requestActivityRecognitionPermission()
    }

    private fun initViews(view: View) {
        mapView = view.findViewById(R.id.mapView)
        tvTime = view.findViewById(R.id.tvTime)
        tvDistance = view.findViewById(R.id.tvDistance)
        tvSpeed = view.findViewById(R.id.tvSpeed)
        tvCalories = view.findViewById(R.id.tvCalories)
        tvGpsStatus = view.findViewById(R.id.tvGpsStatus)
        btnPause = view.findViewById(R.id.btnPause)
        btnStop = view.findViewById(R.id.btnStop)
    }

    private fun setupMap(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 地图基础设置
        map.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.apply {
                isZoomControlsEnabled = false
                isCompassEnabled = true
                isMyLocationButtonEnabled = true
            }
        }

        val context = requireContext()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // 权限检查
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
                } else {
                    val defaultLocation = LatLng(39.9042, 116.4074)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
                }
            }
        } else {
            val defaultLocation = LatLng(39.9042, 116.4074)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
        }
    }

    private fun observeWorkoutData() {
        // 观察时间
        workoutViewModel.time.observe(viewLifecycleOwner) { time ->
            tvTime.text = time
        }

        // 观察距离
        workoutViewModel.distance.observe(viewLifecycleOwner) { distance ->
            tvDistance.text = distance
        }

        // 观察速度
        workoutViewModel.speed.observe(viewLifecycleOwner) { speed ->
            tvSpeed.text = speed
        }

        // 观察卡路里
        workoutViewModel.calories.observe(viewLifecycleOwner) { calories ->
            tvCalories.text = calories
        }

        // 观察GPS状态
        workoutViewModel.debugInfo.observe(viewLifecycleOwner) { info ->
            when {
                info.contains("GPS") -> {
                    tvGpsStatus.text = "GPS"
                    tvGpsStatus.setTextColor(Color.parseColor("#4CAF50"))
                }
                info.contains("Simulator") -> {
                    tvGpsStatus.text = "模拟"
                    tvGpsStatus.setTextColor(Color.parseColor("#2196F3"))
                }
                info.contains("Accelerometer") -> {
                    tvGpsStatus.text = "传感器"
                    tvGpsStatus.setTextColor(Color.parseColor("#FF9800"))
                }
                else -> {
                    tvGpsStatus.text = "定位中"
                    tvGpsStatus.setTextColor(Color.parseColor("#F44336"))
                }
            }
        }
    }

    private fun setupButtons() {
        btnPause.setOnClickListener {
            if (isPaused) {
                // 恢复运动
                resumeWorkout()
            } else {
                // 暂停运动
                pauseWorkout()
            }
        }

        btnStop.setOnClickListener {
            stopWorkout()
        }
    }

    private fun pauseWorkout() {
        isPaused = true
        workoutViewModel.pauseWorkout()
        btnPause.setImageResource(R.drawable.continue_record)
    }

    private fun resumeWorkout() {
        isPaused = false
        workoutViewModel.resumeWorkout()
        btnPause.setImageResource(R.drawable.pause_record)
    }

    private fun stopWorkout() {
        workoutViewModel.pauseWorkout()

        // 跳转到完成页面，传递数据
        val intent = Intent(requireContext(), FinishActivity::class.java).apply {
            putExtra("distance", workoutViewModel.distance.value ?: "0.00 m")
            putExtra("duration", workoutViewModel.time.value ?: "00:00:00")
            putExtra("calories", workoutViewModel.calories.value ?: "0 kcal")
            putExtra("workoutType", workoutViewModel.workoutType.value ?: "Running")
            putExtra("steps", workoutViewModel.steps.value ?: 0)
        }
        startActivity(intent)
        activity?.finish()
    }

    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                if (!isPaused) {
                    workoutViewModel.tick()
                    updateMapRoute()
                }
                handler.postDelayed(this, 200)
            }
        })
    }

    private fun updateMapRoute() {
        val routes = workoutViewModel.getRoutePoints()
        if (routes.size < 2 || googleMap == null) return

        val lastTwo = routes.takeLast(2)
        val start = LatLng(lastTwo[0].lat, lastTwo[0].lng)
        val end = LatLng(lastTwo[1].lat, lastTwo[1].lng)

        // 计算两点距离（米）
        val dist = FloatArray(1)
        android.location.Location.distanceBetween(
            start.latitude, start.longitude, end.latitude, end.longitude, dist
        )
        val distance = dist[0]

        // ✅ 根据距离判断：>50m 画虚线，否则画实线
        val polylineOptions = PolylineOptions()
            .add(start, end)
            .width(8f)
            .color(Color.parseColor("#FF4444"))

        if (distance > 50f) {
            // 🚫 GPS 跳点 → 画虚线
            polylineOptions.pattern(listOf(Dot(), Gap(10f)))
        }

        // ✅ 在地图上添加新线段
        googleMap?.addPolyline(polylineOptions)

        // ✅ 更新 marker
        val latestPoint = end
        if (userMarker == null) {
            userMarker = googleMap?.addMarker(
                MarkerOptions()
                    .position(latestPoint)
                    .title("当前位置")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
        } else {
            userMarker!!.position = latestPoint
        }

        // ✅ 摄像头移动逻辑
        val cameraPos = googleMap?.cameraPosition?.target
        if (cameraPos == null || distanceBetween(cameraPos, latestPoint) > 10) {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latestPoint))
        }
    }


    // 计算两点距离（米）
    private fun distanceBetween(p1: LatLng, p2: LatLng): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            p1.latitude, p1.longitude, p2.latitude, p2.longitude, results
        )
        return results[0]
    }


    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            workoutViewModel.startLocationTracking()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }
    }

    private fun requestActivityRecognitionPermission() {
        val granted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), REQ_ACTIVITY_RECOGNITION)
        } else {
            workoutViewModel.startStepSensors()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // ✅ 这是你原来的定位逻辑
            workoutViewModel.startLocationTracking()
        }

        if (requestCode == REQ_ACTIVITY_RECOGNITION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // ✅ 授权成功 → 现在注册计步传感器
                workoutViewModel.startStepSensors()
                Toast.makeText(requireContext(), "Activity recognition granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permission denied for step detection", Toast.LENGTH_SHORT).show()
            }
        }
    }



    // MapView生命周期管理
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}