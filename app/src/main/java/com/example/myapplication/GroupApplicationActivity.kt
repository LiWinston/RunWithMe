package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

/**
 * Group Application Activity
 * 功能：
 * 1. 查看收到的加入申请（Received Tab）
 * 2. 查看自己的申请历史（Sent Tab）
 */
class GroupApplicationActivity : AppCompatActivity() {

    data class Application(
        val id: String,
        val userId: String,
        val userName: String,
        val userAvatar: Int,
        val groupId: String,
        val groupName: String,
        val timestamp: Long,
        var status: ApplicationStatus,  // 改为 var 以便可以修改状态
        val type: ApplicationType
    )

    enum class ApplicationStatus {
        PENDING, APPROVED, REJECTED
    }

    enum class ApplicationType { RECEIVED }

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: android.widget.ImageButton
    private lateinit var tvEmpty: TextView

    private var currentTab = 0 // only Received remains
    private lateinit var receivedApplications: MutableList<Application>
    // removed sent list per product decision

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_application)

        initMockData()
        initViews()
        setupListeners()
        updateDisplay()
    }

    private fun initMockData() {
        receivedApplications = mutableListOf()
        val api = com.example.myapplication.landr.RetrofitClient.create(com.example.myapplication.group.GroupApi::class.java)
        api.receivedApplications().enqueue(object: retrofit2.Callback<com.example.myapplication.group.Result<List<com.example.myapplication.group.ApplicationItem>>> {
            override fun onResponse(
                call: retrofit2.Call<com.example.myapplication.group.Result<List<com.example.myapplication.group.ApplicationItem>>>,
                response: retrofit2.Response<com.example.myapplication.group.Result<List<com.example.myapplication.group.ApplicationItem>>>
            ) {
                val res = response.body()
                if (response.isSuccessful && res != null && res.code == 0 && res.data != null) {
                    val mapped = res.data.map {
                        Application(
                            id = it.id.toString(),
                            userId = it.userId.toString(),
                            userName = it.userName,
                            userAvatar = R.drawable.ic_profile,
                            groupId = it.groupId.toString(),
                            groupName = it.groupName,
                            timestamp = it.timestamp,
                            status = when (it.status) {
                                "APPROVED" -> ApplicationStatus.APPROVED
                                "REJECTED" -> ApplicationStatus.REJECTED
                                else -> ApplicationStatus.PENDING
                            },
                            type = ApplicationType.RECEIVED
                        )
                    }
                    receivedApplications.clear()
                    receivedApplications.addAll(mapped)
                    updateDisplay()
                } else {
                    updateDisplay()
                }
            }

            override fun onFailure(
                call: retrofit2.Call<com.example.myapplication.group.Result<List<com.example.myapplication.group.ApplicationItem>>>,
                t: Throwable
            ) {
                updateDisplay()
            }
        })
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tab_layout)
        recyclerView = findViewById(R.id.rv_applications)
        btnBack = findViewById(R.id.btn_back)
        tvEmpty = findViewById(R.id.tv_empty)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // 设置TabLayout
    tabLayout.addTab(tabLayout.newTab().setText("Received"))
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                updateDisplay()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateDisplay() {
    val applications = receivedApplications
        
        if (applications.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "No received applications"
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            
            recyclerView.adapter = ApplicationAdapter(
                applications,
                true,
                onApprove = { app -> approveApplication(app) },
                onReject = { app -> rejectApplication(app) }
            )
        }
    }

    private fun approveApplication(application: Application) {
        val api = com.example.myapplication.landr.RetrofitClient.create(com.example.myapplication.group.GroupApi::class.java)
        val body = com.example.myapplication.group.ModerateBody(applicationId = application.id.toLong(), approve = true, reason = null)
        api.moderate(body).enqueue(object: retrofit2.Callback<com.example.myapplication.group.Result<String>> {
            override fun onResponse(
                call: retrofit2.Call<com.example.myapplication.group.Result<String>>,
                response: retrofit2.Response<com.example.myapplication.group.Result<String>>
            ) {
                if (response.isSuccessful && response.body()?.code == 0) {
                    Toast.makeText(this@GroupApplicationActivity, "Approved ${application.userName}", Toast.LENGTH_SHORT).show()
                    application.status = ApplicationStatus.APPROVED
                    updateDisplay()
                } else {
                    Toast.makeText(this@GroupApplicationActivity, response.body()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(
                call: retrofit2.Call<com.example.myapplication.group.Result<String>>,
                t: Throwable
            ) {
                Toast.makeText(this@GroupApplicationActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun rejectApplication(application: Application) {
        val api = com.example.myapplication.landr.RetrofitClient.create(com.example.myapplication.group.GroupApi::class.java)
        val body = com.example.myapplication.group.ModerateBody(applicationId = application.id.toLong(), approve = false, reason = null)
        api.moderate(body).enqueue(object: retrofit2.Callback<com.example.myapplication.group.Result<String>> {
            override fun onResponse(
                call: retrofit2.Call<com.example.myapplication.group.Result<String>>,
                response: retrofit2.Response<com.example.myapplication.group.Result<String>>
            ) {
                if (response.isSuccessful && response.body()?.code == 0) {
                    Toast.makeText(this@GroupApplicationActivity, "Rejected ${application.userName}", Toast.LENGTH_SHORT).show()
                    application.status = ApplicationStatus.REJECTED
                    updateDisplay()
                } else {
                    Toast.makeText(this@GroupApplicationActivity, response.body()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(
                call: retrofit2.Call<com.example.myapplication.group.Result<String>>,
                t: Throwable
            ) {
                Toast.makeText(this@GroupApplicationActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // RecyclerView Adapter
    inner class ApplicationAdapter(
        private val applications: List<Application>,
        private val showActions: Boolean,
        private val onApprove: (Application) -> Unit,
        private val onReject: (Application) -> Unit
    ) : RecyclerView.Adapter<ApplicationAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivAvatar: ImageView = view.findViewById(R.id.iv_app_avatar)
            val tvName: TextView = view.findViewById(R.id.tv_app_name)
            val tvGroup: TextView = view.findViewById(R.id.tv_app_group)
            val tvTime: TextView = view.findViewById(R.id.tv_app_time)
            val tvStatus: TextView = view.findViewById(R.id.tv_app_status)
            val layoutActions: LinearLayout = view.findViewById(R.id.layout_actions)
            val btnApprove: Button = view.findViewById(R.id.btn_approve)
            val btnReject: Button = view.findViewById(R.id.btn_reject)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_application, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = applications[position]

            holder.ivAvatar.setImageResource(app.userAvatar)
            
            // Received: show applicant name
            holder.tvName.text = app.userName
            holder.tvGroup.text = "wants to join ${app.groupName}"
            
            holder.tvTime.text = formatTimestamp(app.timestamp)

            // 显示状态或操作按钮
            when (app.status) {
                ApplicationStatus.PENDING -> {
                    if (showActions) {
                        // Received tab: 显示批准/拒绝按钮
                        holder.tvStatus.visibility = View.GONE
                        holder.layoutActions.visibility = View.VISIBLE
                        
                        holder.btnApprove.setOnClickListener {
                            onApprove(app)
                        }
                        
                        holder.btnReject.setOnClickListener {
                            onReject(app)
                        }
                    }
                }
                ApplicationStatus.APPROVED -> {
                    holder.tvStatus.visibility = View.VISIBLE
                    holder.layoutActions.visibility = View.GONE
                    holder.tvStatus.text = "Approved"
                    holder.tvStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                }
                ApplicationStatus.REJECTED -> {
                    holder.tvStatus.visibility = View.VISIBLE
                    holder.layoutActions.visibility = View.GONE
                    holder.tvStatus.text = "Rejected"
                    holder.tvStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                }
            }
        }

        override fun getItemCount() = applications.size

        private fun formatTimestamp(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return when {
                days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
                hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
                else -> "Just now"
            }
        }
    }
}

