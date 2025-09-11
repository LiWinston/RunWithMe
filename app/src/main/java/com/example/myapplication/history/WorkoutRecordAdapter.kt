package com.example.myapplication.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        private val tvWorkoutType: TextView = itemView.findViewById(R.id.tvWorkoutType)
        private val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        private val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvCalories: TextView = itemView.findViewById(R.id.tvCalories)
        private val tvPace: TextView = itemView.findViewById(R.id.tvPace)

        fun bind(workout: Workout) {
            // 调试信息 - 打印每个workout的信息
            android.util.Log.d("WorkoutRecordAdapter", "绑定Workout ID: ${workout.id}, 类型: ${workout.workoutType}, 开始时间: ${workout.startTime}")
            
            // 运动类型
            tvWorkoutType.text = when (workout.workoutType) {
                "OUTDOOR_RUN" -> "户外跑步"
                "TREADMILL" -> "跑步机"
                "WALK" -> "步行"
                "CYCLING" -> "骑行"
                "SWIMMING" -> "游泳"
                else -> "其他运动"
            }

            // 开始时间 - 将UTC时间转换为本地时间
            try {
                val utcTime = LocalDateTime.parse(workout.startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val localTime = utcTime.atZone(java.time.ZoneId.of("UTC"))
                    .withZoneSameInstant(java.time.ZoneId.systemDefault())
                    .toLocalDateTime()
                
                // 调试信息
                android.util.Log.d("WorkoutRecordAdapter", "原始UTC时间: ${workout.startTime}, 转换后本地时间: $localTime, 系统时区: ${java.time.ZoneId.systemDefault()}")
                
                tvStartTime.text = localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) {
                android.util.Log.e("WorkoutRecordAdapter", "时间解析失败: ${workout.startTime}", e)
                tvStartTime.text = "时间错误"
            }

            // 距离
            val distance = try {
                workout.distance?.toDouble() ?: 0.0
            } catch (e: NumberFormatException) {
                0.0
            }
            tvDistance.text = String.format("%.2f km", distance)

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
            val calories = try {
                workout.calories?.toDouble()?.toInt() ?: 0
            } catch (e: NumberFormatException) {
                0
            }
            tvCalories.text = "${calories} kcal"

            // 配速 - 优先使用avgPace，如果没有则从avgSpeed计算
            val pace = when {
                workout.avgPace != null && workout.avgPace > 0 -> workout.avgPace
                workout.avgSpeed != null -> {
                    try {
                        val speedKmh = workout.avgSpeed.toDouble()
                        if (speedKmh > 0) (3600 / speedKmh).toInt() else 0 // 从速度计算配速
                    } catch (e: NumberFormatException) {
                        0
                    }
                }
                else -> 0
            }
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
