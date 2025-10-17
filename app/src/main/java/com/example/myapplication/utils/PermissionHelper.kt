package com.example.myapplication.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHelper {
    
    /**
     * Check if location permission is granted
     */
    fun isLocationPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if notifications are enabled (preference)
     */
    fun isNotificationsEnabled(context: Context): Boolean {
        return context.getSharedPreferences("app_permissions", Context.MODE_PRIVATE)
            .getBoolean("notifications_enabled", true)
    }
    
    /**
     * Check if location is enabled (preference)
     */
    fun isLocationEnabled(context: Context): Boolean {
        return context.getSharedPreferences("app_permissions", Context.MODE_PRIVATE)
            .getBoolean("location_enabled", true)
    }
}

