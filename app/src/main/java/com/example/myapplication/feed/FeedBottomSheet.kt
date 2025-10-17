package com.example.myapplication.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.group.FeedResponse
import com.example.myapplication.group.GroupApi
import com.example.myapplication.group.NotificationItem
import com.example.myapplication.landr.RetrofitClient
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FeedBottomSheet: BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottomsheet_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.feedRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        val api = RetrofitClient.create(GroupApi::class.java)
        api.feed(50).enqueue(object: retrofit2.Callback<com.example.myapplication.group.Result<FeedResponse>>{
            override fun onResponse(
                call: retrofit2.Call<com.example.myapplication.group.Result<FeedResponse>>,
                response: retrofit2.Response<com.example.myapplication.group.Result<FeedResponse>>
            ) {
                val res = response.body()
                if (response.isSuccessful && res != null && res.code == 0 && res.data != null) {
                    val feed = res.data
                    val items = mutableListOf<Pair<String,String>>()
                    feed.workouts?.forEach { w ->
                        val time = w.startTime ?: ""
                        val summary = buildString {
                            append("🏃 ")
                            append((w.distance ?: 0.0).let { String.format("%.1f km", it) })
                            if (!w.workoutType.isNullOrBlank()) append(" · ").append(w.workoutType)
                        }
                        items += time to summary
                    }
                    feed.interactions?.forEach { n ->
                        val time = n.createdAt ?: ""
                        val summary = when (n.type) {
                            "LIKE" -> "👍 ${n.actorUserId ?: "Someone"} → ${n.targetUserId ?: "Someone"}"
                            "REMIND" -> "⏰ ${n.actorUserId ?: "Someone"} → ${n.targetUserId ?: "Someone"}"
                            "WEEKLY_GOAL_ACHIEVED" -> "🎯 User ${n.actorUserId ?: ""} reached weekly goal"
                            else -> n.type
                        }
                        items += time to summary
                    }
                    // 简单按时间字符串降序（后端已排序，这里作为兜底）
                    val sorted = items.sortedByDescending { it.first }
                    recycler.adapter = FeedAdapter(sorted)
                }
            }

            override fun onFailure(
                call: retrofit2.Call<com.example.myapplication.group.Result<FeedResponse>>,
                t: Throwable
            ) { /* ignore */ }
        })
    }

    class FeedAdapter(private val items: List<Pair<String,String>>) : RecyclerView.Adapter<FeedAdapter.VH>() {
        class VH(v: View): RecyclerView.ViewHolder(v) {
            val left: TextView = v.findViewById(R.id.feedDate)
            val right: TextView = v.findViewById(R.id.feedSummary)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_row, parent, false)
            return VH(v)
        }
        override fun getItemCount() = items.size
        override fun onBindViewHolder(holder: VH, position: Int) {
            val (d,s) = items[position]
            holder.left.text = if (d.length >= 16) d.substring(5,16).replace('T',' ') else d
            holder.right.text = s
        }
    }
}
