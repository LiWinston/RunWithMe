package com.example.myapplication.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.android.material.tabs.TabLayout

/**
 * 运动历史记录Fragment
 * 包含Today/Week/Month三个Tab
 */
class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载布局
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            setupTabLayout(view)

            // 默认显示今日数据
            if (savedInstanceState == null) {
                // 使用 post 延迟执行，确保 Fragment 已经完全附加到 Activity
                view.post {
                    if (isAdded && !isDetached) {
                        showTodayFragment()
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HistoryFragment", "Error in onViewCreated", e)
        }
    }

    private fun setupTabLayout(rootView: View) {
        val tabLayout = rootView.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showTodayFragment()
                    1 -> showWeekFragment()
                    2 -> showMonthFragment()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        }) ?: android.util.Log.e("HistoryFragment", "TabLayout not found!")
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
        try {
            if (isAdded && !isDetached) {
                childFragmentManager.beginTransaction()
                    .replace(R.id.historyContainer, fragment)
                    .commitAllowingStateLoss()
            } else {
                android.util.Log.w("HistoryFragment", "Cannot replace fragment - not attached to activity")
            }
        } catch (e: Exception) {
            android.util.Log.e("HistoryFragment", "Error replacing fragment", e)
        }
    }
}