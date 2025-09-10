package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.history.HistoryActivity

class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 直接启动HistoryActivity
        val intent = Intent(requireContext(), HistoryActivity::class.java)
        startActivity(intent)
        
        // 返回一个空的视图，因为我们要跳转到新的Activity
        return inflater.inflate(R.layout.fragment_history, container, false)
    }
}