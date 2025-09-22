package com.example.myapplication.record

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class StartRecordFragment : Fragment(R.layout.fragment_start_record) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 开始按钮 → 跳到 PreRecordActivity（倒计时）
        val btnRecord = view.findViewById<Button>(R.id.btn_record)
        btnRecord.setOnClickListener {
            val intent = Intent(requireContext(), PreRecordActivity::class.java)
            startActivity(intent)
        }
    }
}
