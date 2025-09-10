package com.example.myapplication.history

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.android.material.tabs.TabLayout

/**
 * 运动历史记录Activity
 * 包含Today/Week/Month三个Tab
 */
class HistoryActivity : AppCompatActivity() {

    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        setupTabLayout()
        
        // 默认显示今日数据
        if (savedInstanceState == null) {
            showTodayFragment()
        }
    }

    private fun setupTabLayout() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showTodayFragment()
                    1 -> showWeekFragment()
                    2 -> showMonthFragment()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showTodayFragment() {
        replaceFragment(HistoryTodayFragment())
    }

    private fun showWeekFragment() {
        replaceFragment(HistoryWeekFragment())
    }

    private fun showMonthFragment() {
        replaceFragment(HistoryMonthFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.historyContainer, fragment)
            .commit()
    }
}
