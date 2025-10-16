package com.example.myapplication

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
            Toast.makeText(context, "Exercise Recording clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to exercise records with details (distance, time, calories, single record browsing)
        }
        
        // Goals
        view.findViewById<TextView>(R.id.set_slogan).setOnClickListener {
            Toast.makeText(context, "Set Slogan clicked", Toast.LENGTH_SHORT).show()
            // TODO: Show dialog to set personal slogan
        }
        
        view.findViewById<TextView>(R.id.create_goal).setOnClickListener {
            Toast.makeText(context, "Create Goal clicked", Toast.LENGTH_SHORT).show()
            // TODO: Show dialog to create goal with time span (weekly/monthly)
        }
        
        // Personal Profile - Change Password
        view.findViewById<TextView>(R.id.change_password).setOnClickListener {
            Toast.makeText(context, "Change Password clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to change password page
        }
        
        // Permissions
        view.findViewById<TextView>(R.id.privacy_settings).setOnClickListener {
            Toast.makeText(context, "Privacy Settings clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to privacy settings (default sharing permission for messages)
        }
        
        // Help
        view.findViewById<TextView>(R.id.contact_support).setOnClickListener {
            Toast.makeText(context, "Contact Support clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to contact support page (describe issue + add image)
        }
        
        view.findViewById<TextView>(R.id.faq).setOnClickListener {
            Toast.makeText(context, "FAQ clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to FAQ page
        }
        
        // Logout Button
        view.findViewById<Button>(R.id.btn_logout).setOnClickListener {
            logout()
        }
    }
    
    private fun setupSwitches(view: View) {
        val notificationSwitch = view.findViewById<Switch>(R.id.switch_notifications)
        val voiceSwitch = view.findViewById<Switch>(R.id.switch_voice)
        val locationSwitch = view.findViewById<Switch>(R.id.switch_location)
        
        // Push Notifications
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(context, "Push Notifications ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
            // TODO: Update notification preference in backend
        }
        
        // Voice Permission
        voiceSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(context, "Voice ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
            // TODO: Request/revoke voice permission
        }
        
        // Location/GPS Permission
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(context, "Location/GPS ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
            // TODO: Request/revoke location permission
        }
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
