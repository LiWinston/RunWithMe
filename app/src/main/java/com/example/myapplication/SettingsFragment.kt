package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.landr.RetrofitClient
import com.example.myapplication.landr.TokenManager
import com.example.myapplication.landr.loginapp.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    
    private lateinit var tokenManager: TokenManager
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        tokenManager = TokenManager.getInstance(requireContext())
        
        setupClickListeners(view)
        setupSwitches(view)
    }
    
    private fun setupClickListeners(view: View) {
        // Personal Profile
        view.findViewById<TextView>(R.id.view_profile).setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }
        
        view.findViewById<TextView>(R.id.exercise_records).setOnClickListener {
            val intent = Intent(requireContext(), ExerciseRecordingActivity::class.java)
            startActivity(intent)
        }
        
        // Goals
        view.findViewById<TextView>(R.id.set_slogan).setOnClickListener {
            val intent = Intent(requireContext(), SetSloganActivity::class.java)
            startActivity(intent)
        }
        
        view.findViewById<TextView>(R.id.create_goal).setOnClickListener {
            val intent = Intent(requireContext(), AdjustGoalActivity::class.java)
            startActivity(intent)
        }
        
        // Personal Profile - Change Password
        view.findViewById<TextView>(R.id.change_password).setOnClickListener {
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }
        
        // Logout Button
        view.findViewById<Button>(R.id.btn_logout).setOnClickListener {
            showLogoutConfirmation()
        }
    }
    
    private fun setupSwitches(view: View) {
        val notificationSwitch = view.findViewById<Switch>(R.id.switch_notifications)
        val locationSwitch = view.findViewById<Switch>(R.id.switch_location)
        
        // Load saved preferences
        val sharedPreferences = requireContext().getSharedPreferences("app_permissions", Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        val locationEnabled = sharedPreferences.getBoolean("location_enabled", true)
        
        notificationSwitch.isChecked = notificationsEnabled
        locationSwitch.isChecked = locationEnabled
        
        // Push Notifications
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply()
            Toast.makeText(context, "Push Notifications ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
            
            // Update notification channel if needed
            if (isChecked) {
                // Enable notifications
                enableNotifications()
            } else {
                // Disable notifications
                disableNotifications()
            }
        }
        
        // Location/GPS Permission
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("location_enabled", isChecked).apply()
            Toast.makeText(context, "Location/GPS ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
            
            if (isChecked) {
                // Request location permission if not granted
                requestLocationPermission()
            }
        }
    }
    
    private fun enableNotifications() {
        // Enable notification channels
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channel = android.app.NotificationChannel(
                "default_channel",
                "General Notifications",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun disableNotifications() {
        // Note: We can't fully disable notifications programmatically
        // Users need to do this in system settings
        // We just update our app preference
    }
    
    private fun requestLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Location permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
                // Update switch state
                val locationSwitch = view?.findViewById<Switch>(R.id.switch_location)
                locationSwitch?.isChecked = false
                requireContext().getSharedPreferences("app_permissions", Context.MODE_PRIVATE)
                    .edit().putBoolean("location_enabled", false).apply()
            }
        }
    }
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
    
    private fun showLogoutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = tokenManager.getAccessToken()
                if (accessToken != null) {
                    RetrofitClient.api.logout("Bearer $accessToken")
                }
            } catch (e: Exception) {
                // Ignore network errors, just clear local token
            } finally {
                tokenManager.clearTokens()
                activity?.runOnUiThread {
                    navigateToLogin()
                }
            }
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}
