package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.landr.RetrofitClient
import com.example.myapplication.landr.TokenManager
import com.example.myapplication.landr.loginapp.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化
        RetrofitClient.init(this)
        tokenManager = TokenManager.getInstance(this)
        
        // 检查登录状态
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin()
            return
        }
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.main, HomeFragment())
            .commit()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_search -> SearchFragment()
                R.id.nav_profile -> ProfileFragment()
                R.id.nav_publish -> PublishFragment()
                else -> null
            }
            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main, it)
                    .commit()
            }
            true
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
                // 忽略网络错误，直接清除本地token
            } finally {
                tokenManager.clearTokens()
                runOnUiThread {
                    navigateToLogin()
                }
            }
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}