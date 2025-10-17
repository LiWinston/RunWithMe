package com.example.myapplication.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.record.Workout
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 运动记录列表适配器
 * 显示运动记录列表，支持点击展开详情
 */
class WorkoutRecordAdapter(
    private val onItemClick: (Workout) -> Unit
) : ListAdapter<Workout, WorkoutRecordAdapter.WorkoutViewHolder>(WorkoutDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_record, parent, false)
        return WorkoutViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WorkoutViewHolder(
        itemView: View,
        private val onItemClick: (Workout) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivWorkoutIcon: ImageView = itemView.findViewById(R.id.ivWorkoutIcon)
        private val tvWorkoutType: TextView = itemView.findViewById(R.id.tvWorkoutType)
        private val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        private val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvCalories: TextView = itemView.findViewById(R.id.tvCalories)
        private val tvPace: TextView = itemView.findViewById(R.id.tvPace)

        fun bind(workout: Workout) {
            // Debug info - print each workout information
            android.util.Log.d("WorkoutRecordAdapter", "Binding Workout ID: ${workout.id}, Type: ${workout.workoutType}, Start Time: ${workout.startTime}")
            
            // Workout type and icon
            when (workout.workoutType) {
                "Walking", "WALK" -> {
                    tvWorkoutType.text = "Walking"
                    ivWorkoutIcon.setImageResource(R.drawable.walking)
                }
                "Brisk Walking" -> {
                    tvWorkoutType.text = "Brisk Walking"
                    ivWorkoutIcon.setImageResource(R.drawable.walking)
                }
                "Jogging" -> {
                    tvWorkoutType.text = "Jogging"
                    ivWorkoutIcon.setImageResource(R.drawable.jogging)
                }
                "Running", "OUTDOOR_RUN", "TREADMILL" -> {
                    tvWorkoutType.text = "Running"
                    ivWorkoutIcon.setImageResource(R.drawable.running)
                }
                "Fast Running" -> {
                    tvWorkoutType.text = "Fast Running"
                    ivWorkoutIcon.setImageResource(R.drawable.running)
                }
                else -> {
                    tvWorkoutType.text = "Other"
                    ivWorkoutIcon.setImageResource(R.drawable.running) // Default to running icon
                }
            }

            // Start time - parse backend local time (supports both 'yyyy-MM-dd HH:mm:ss' and ISO_LOCAL_DATE_TIME)
            val parsedLocal: LocalDateTime? = try {
                // Try MySQL-like format first
                LocalDateTime.parse(workout.startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            } catch (e1: Exception) {
                try {
                    LocalDateTime.parse(workout.startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } catch (e2: Exception) {
                    null
                }
            }
            if (parsedLocal != null) {
                tvStartTime.text = parsedLocal.format(DateTimeFormatter.ofPattern("HH:mm"))
            } else {
                android.util.Log.e("WorkoutRecordAdapter", "Time parsing failed: ${workout.startTime}")
                tvStartTime.text = "--:--"
            }

            // Distance
            val distance = try {
                workout.distance?.toDouble() ?: 0.0
            } catch (e: NumberFormatException) {
                0.0
            }
            tvDistance.text = String.format("%.2f km", distance)

            // Duration
            val duration = workout.duration ?: 0
            val hours = duration / 3600
            val minutes = (duration % 3600) / 60
            val durationText = if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
            tvDuration.text = durationText

            // Calories
            val calories = try {
                workout.calories?.toDouble()?.toInt() ?: 0
            } catch (e: NumberFormatException) {
                0
            }
            tvCalories.text = "${calories} kcal"

            // Pace - prioritize avgPace, calculate from avgSpeed if not available
            val pace = when {
                workout.avgPace != null && workout.avgPace > 0 -> workout.avgPace
                workout.avgSpeed != null -> {
                    try {
                        val speedKmh = workout.avgSpeed.toDouble()
                        if (speedKmh > 0) (3600 / speedKmh).toInt() else 0 // Calculate pace from speed
                    } catch (e: NumberFormatException) {
                        0
                    }
                }
                else -> 0
            }
            val paceMinutes = pace / 60
            val paceSeconds = pace % 60
            tvPace.text = "${paceMinutes}'${String.format("%02d", paceSeconds)}\""

            // Click event
            itemView.setOnClickListener {
                onItemClick(workout)
            }
        }
    }

    class WorkoutDiffCallback : DiffUtil.ItemCallback<Workout>() {
        override fun areItemsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem == newItem
        }
    }
}
