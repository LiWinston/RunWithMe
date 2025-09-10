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
import android.widget.Button
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button

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
        
        // 配置地图
        map.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.apply {
                isZoomControlsEnabled = false
                isCompassEnabled = true
                isMyLocationButtonEnabled = false
            }
            
            // 检查位置权限
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                isMyLocationEnabled = true
            }
        }
        
        // 设置初始位置（北京）
        val defaultLocation = LatLng(39.9042, 116.4074)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
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
            workoutViewModel.pauseWorkout()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, PauseFragment())
                .commit()
        }
        
        btnStop.setOnClickListener {
            stopWorkout()
        }
    }
    
    private fun stopWorkout() {
        workoutViewModel.pauseWorkout()
        
        // 跳转到完成页面，传递数据
        val intent = Intent(requireContext(), FinishActivity::class.java).apply {
            putExtra("distance", workoutViewModel.distance.value ?: "0.00 miles")
            putExtra("duration", workoutViewModel.time.value ?: "00:00:00")
            putExtra("calories", workoutViewModel.calories.value ?: "0 kcal")
            putExtra("speed", workoutViewModel.speed.value ?: "0.00 mph")
        }
        startActivity(intent)
        activity?.finish()
    }
    
    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                workoutViewModel.tick()
                updateMapRoute()
                handler.postDelayed(this, 1000)
            }
        })
    }
    
    private fun updateMapRoute() {
        val routes = workoutViewModel.getRoutePoints()
        if (routes.isNotEmpty() && googleMap != null) {
            val newRoutePoints = routes.map { LatLng(it.latitude, it.longitude) }
            
            // 更新路线
            routePolyline?.remove()
            routePolyline = googleMap?.addPolyline(
                PolylineOptions()
                    .addAll(newRoutePoints)
                    .color(Color.parseColor("#FF4444"))
                    .width(8f)
                    .pattern(listOf(Dash(20f), Gap(10f)))
            )
            
            // 更新用户位置标记
            if (newRoutePoints.isNotEmpty()) {
                val currentLocation = newRoutePoints.last()
                
                userMarker?.remove()
                userMarker = googleMap?.addMarker(
                    MarkerOptions()
                        .position(currentLocation)
                        .title("当前位置")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )
                
                // 移动摄像头跟随用户
                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(currentLocation, 17f)
                )
            }
        }
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
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            workoutViewModel.startLocationTracking()
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