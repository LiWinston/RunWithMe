package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class WorkoutFragment_1 : Fragment(R.layout.fragment_workout_1), OnMapReadyCallback {

    private var hasNavigated = false
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    // 用于存储轨迹点
    private val trackPoints = mutableListOf<LatLng>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 获取地图 Fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment)
        if (mapFragment is SupportMapFragment) {
            mapFragment.getMapAsync(this)
        }

        // 按钮点击跳转
        val button = view.findViewById<ImageView>(R.id.start_button)
        button.setOnClickListener {
            if (!hasNavigated) {
                hasNavigated = true
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main, WorkoutFragment_2())
                    .addToBackStack(null)
                    .commit()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // 检查权限并启动位置更新
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    // 地图准备就绪
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // 启用蓝点显示当前位置
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        }
    }

    // 实时位置更新
    private fun startLocationUpdates() {
        // 初始化类成员 locationRequest
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L // 每 3 秒更新一次
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    Log.d("Location", "Lat: ${location.latitude}, Lng: ${location.longitude}")

                    if (::map.isInitialized) {
                        // 保存轨迹点
                        trackPoints.add(currentLatLng)

                        // 清空地图并重绘 Marker + Polyline
                        map.clear()

                        // Marker 表示当前位置
                        map.addMarker(
                            MarkerOptions().position(currentLatLng).title("当前位置")
                        )

                        // 绘制轨迹
                        map.addPolyline(
                            PolylineOptions()
                                .addAll(trackPoints)
                                .width(6f)
                                .color(Color.RED)
                                .geodesic(true)
                        )

                        // 移动摄像机到当前位置
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    }
                }
            }
        }

        // 请求位置更新
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 停止更新，防止内存泄漏
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}