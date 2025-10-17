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
    private var workoutStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RecordingFragment())
                .commit()
        }

        // Check and request permission first, then start workout
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, start workout
            startWorkoutIfNeeded()
        } else {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                101
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == 101) {
            // Permission granted or denied, start workout anyway
            // (step counting will work if granted, otherwise just won't count steps)
            startWorkoutIfNeeded()
        }
    }

    private fun startWorkoutIfNeeded() {
        if (!workoutStarted) {
            workoutStarted = true
            workoutViewModel.startWorkout()
        }
    }
}