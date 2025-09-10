package com.example.myapplication.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
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

        private val tvWorkoutType: TextView = itemView.findViewById(R.id.tvWorkoutType)
        private val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        private val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvCalories: TextView = itemView.findViewById(R.id.tvCalories)
        private val tvPace: TextView = itemView.findViewById(R.id.tvPace)

        fun bind(workout: Workout) {
            // 运动类型
            tvWorkoutType.text = when (workout.workoutType) {
                "OUTDOOR_RUN" -> "户外跑步"
                "TREADMILL" -> "跑步机"
                "WALK" -> "步行"
                "CYCLING" -> "骑行"
                "SWIMMING" -> "游泳"
                else -> "其他运动"
            }

            // 开始时间
            val startTime = LocalDateTime.parse(workout.startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            tvStartTime.text = startTime.format(DateTimeFormatter.ofPattern("HH:mm"))

            // 距离
            tvDistance.text = String.format("%.2f km", workout.distance?.toDouble() ?: 0.0)

            // 持续时间
            val duration = workout.duration ?: 0
            val hours = duration / 3600
            val minutes = (duration % 3600) / 60
            val durationText = if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
            tvDuration.text = durationText

            // 卡路里
            tvCalories.text = "${workout.calories?.toInt() ?: 0} kcal"

            // 配速
            val pace = workout.avgPace ?: 0
            val paceMinutes = pace / 60
            val paceSeconds = pace % 60
            tvPace.text = "${paceMinutes}'${String.format("%02d", paceSeconds)}\""

            // 点击事件
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
