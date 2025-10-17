package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.landr.workout.Workout
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WorkoutAdapter(private val workouts: List<Workout>) :
    RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tv_workout_date)
        val tvType: TextView = view.findViewById(R.id.tv_workout_type)
        val tvDuration: TextView = view.findViewById(R.id.tv_workout_duration)
        val tvDistance: TextView = view.findViewById(R.id.tv_workout_distance)
        val tvPace: TextView = view.findViewById(R.id.tv_workout_pace)
        val tvCalories: TextView = view.findViewById(R.id.tv_workout_calories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]

        // Format date
        try {
            val dateTime = LocalDateTime.parse(workout.startTime)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            holder.tvDate.text = dateTime.format(formatter)
        } catch (e: Exception) {
            holder.tvDate.text = workout.startTime.substring(0, 10)
        }

        // Workout type
        holder.tvType.text = formatWorkoutType(workout.workoutType)

        // Duration
        val duration = workout.duration ?: 0
        val hours = duration / 3600
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60
        holder.tvDuration.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        // Distance
        val distance = workout.distance ?: BigDecimal.ZERO
        holder.tvDistance.text = String.format("%.2f km", distance)

        // Average Pace
        val pace = workout.avgPace ?: 0
        if (pace > 0) {
            val paceMinutes = pace / 60
            val paceSeconds = pace % 60
            holder.tvPace.text = String.format("%d'%02d\" /km", paceMinutes, paceSeconds)
        } else {
            holder.tvPace.text = "N/A"
        }

        // Calories
        val calories = workout.calories ?: BigDecimal.ZERO
        holder.tvCalories.text = String.format("%.0f kcal", calories)
    }

    override fun getItemCount() = workouts.size

    private fun formatWorkoutType(type: String): String {
        return when (type) {
            "OUTDOOR_RUN" -> "Outdoor Run"
            "TREADMILL" -> "Treadmill"
            "WALK" -> "Walk"
            "CYCLING" -> "Cycling"
            "SWIMMING" -> "Swimming"
            "OTHER" -> "Other"
            else -> type
        }
    }
}

