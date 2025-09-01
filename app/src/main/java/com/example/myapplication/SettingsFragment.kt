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
        // Account Settings
        view.findViewById<TextView>(R.id.edit_profile).setOnClickListener {
            Toast.makeText(context, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement edit profile functionality
        }
        
        view.findViewById<TextView>(R.id.change_password).setOnClickListener {
            Toast.makeText(context, "Change Password clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement change password functionality
        }
        
        // App Settings
        view.findViewById<TextView>(R.id.measurement_units).setOnClickListener {
            Toast.makeText(context, "Measurement Units clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement units selection dialog
        }
        
        // Privacy & Support
        view.findViewById<TextView>(R.id.privacy_policy).setOnClickListener {
            Toast.makeText(context, "Privacy Policy clicked", Toast.LENGTH_SHORT).show()
            // TODO: Open privacy policy
        }
        
        view.findViewById<TextView>(R.id.terms_of_service).setOnClickListener {
            Toast.makeText(context, "Terms of Service clicked", Toast.LENGTH_SHORT).show()
            // TODO: Open terms of service
        }
        
        view.findViewById<TextView>(R.id.help_support).setOnClickListener {
            Toast.makeText(context, "Help & Support clicked", Toast.LENGTH_SHORT).show()
            // TODO: Open help & support
        }
        
        view.findViewById<TextView>(R.id.about).setOnClickListener {
            Toast.makeText(context, "About clicked", Toast.LENGTH_SHORT).show()
            // TODO: Show about dialog
        }
        
        // Logout Button
        view.findViewById<Button>(R.id.btn_logout).setOnClickListener {
            logout()
        }
    }
    
    private fun setupSwitches(view: View) {
        val notificationSwitch = view.findViewById<Switch>(R.id.switch_notifications)
        val darkModeSwitch = view.findViewById<Switch>(R.id.switch_dark_mode)
        
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(context, "Notifications ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
            // TODO: Implement notification settings
        }
        
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(context, "Dark Mode ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
            // TODO: Implement dark mode toggle
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
