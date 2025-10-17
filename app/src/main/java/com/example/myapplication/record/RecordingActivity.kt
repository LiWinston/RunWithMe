package com.example.myapplication.record

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.record.RecordingFragment
import com.example.myapplication.record.WorkoutViewModel
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager


class RecordingActivity : AppCompatActivity() {

    private val workoutViewModel: WorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                101
            )
        }

        workoutViewModel.startWorkout()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RecordingFragment())
                .commit()
        }
    }
}