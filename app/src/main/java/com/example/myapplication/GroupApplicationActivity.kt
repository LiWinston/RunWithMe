package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

    enum class ApplicationType {
        RECEIVED, SENT
    }

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: Button
    private lateinit var tvEmpty: TextView

    private var currentTab = 0 // 0: Received, 1: Sent
    private lateinit var receivedApplications: MutableList<Application>
    private lateinit var sentApplications: MutableList<Application>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_application)

        initMockData()
        initViews()
        setupListeners()
        updateDisplay()
    }

    private fun initMockData() {
        // Mock received applications
        receivedApplications = mutableListOf(
            Application(
                id = "app1",
                userId = "user1",
                userName = "Alice",
                userAvatar = R.drawable.ic_profile,
                groupId = "group1",
                groupName = "My Running Group",
                timestamp = System.currentTimeMillis() - 3600000,
                status = ApplicationStatus.PENDING,
                type = ApplicationType.RECEIVED
            ),
            Application(
                id = "app2",
                userId = "user2",
                userName = "Bob",
                userAvatar = R.drawable.ic_profile,
                groupId = "group1",
                groupName = "My Running Group",
                timestamp = System.currentTimeMillis() - 7200000,
                status = ApplicationStatus.PENDING,
                type = ApplicationType.RECEIVED
            )
        )

        // Mock sent applications
        sentApplications = mutableListOf(
            Application(
                id = "app3",
                userId = "me",
                userName = "Me",
                userAvatar = R.drawable.ic_profile,
                groupId = "group2",
                groupName = "Elite Runners",
                timestamp = System.currentTimeMillis() - 86400000,
                status = ApplicationStatus.PENDING,
                type = ApplicationType.SENT
            ),
            Application(
                id = "app4",
                userId = "me",
                userName = "Me",
                userAvatar = R.drawable.ic_profile,
                groupId = "group3",
                groupName = "Morning Joggers",
                timestamp = System.currentTimeMillis() - 172800000,
                status = ApplicationStatus.APPROVED,
                type = ApplicationType.SENT
            ),
            Application(
                id = "app5",
                userId = "me",
                userName = "Me",
                userAvatar = R.drawable.ic_profile,
                groupId = "group4",
                groupName = "Weekend Warriors",
                timestamp = System.currentTimeMillis() - 259200000,
                status = ApplicationStatus.REJECTED,
                type = ApplicationType.SENT
            )
        )

        // TODO: 从数据库获取真实数据
        // receivedApplications = fetchReceivedApplications()
        // sentApplications = fetchSentApplications()
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tab_layout)
        recyclerView = findViewById(R.id.rv_applications)
        btnBack = findViewById(R.id.btn_back)
        tvEmpty = findViewById(R.id.tv_empty)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // 设置TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Received"))
        tabLayout.addTab(tabLayout.newTab().setText("Sent"))
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
        val applications = if (currentTab == 0) receivedApplications else sentApplications
        
        if (applications.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = if (currentTab == 0) {
                "No received applications"
            } else {
                "No sent applications"
            }
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            
            recyclerView.adapter = ApplicationAdapter(
                applications,
                currentTab == 0,
                onApprove = { app -> approveApplication(app) },
                onReject = { app -> rejectApplication(app) }
            )
        }
    }

    private fun approveApplication(application: Application) {
        // TODO: 调用数据库API批准申请
        Toast.makeText(this, "Approved ${application.userName}", Toast.LENGTH_SHORT).show()
        
        application.status = ApplicationStatus.APPROVED
        updateDisplay()
    }

    private fun rejectApplication(application: Application) {
        // TODO: 调用数据库API拒绝申请
        Toast.makeText(this, "Rejected ${application.userName}", Toast.LENGTH_SHORT).show()
        
        application.status = ApplicationStatus.REJECTED
        updateDisplay()
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
            
            if (currentTab == 0) {
                // Received: 显示申请人名字
                holder.tvName.text = app.userName
                holder.tvGroup.text = "wants to join ${app.groupName}"
            } else {
                // Sent: 显示group名字
                holder.tvName.text = app.groupName
                holder.tvGroup.text = "Your application"
            }
            
            holder.tvTime.text = formatTimestamp(app.timestamp)

            // 显示状态或操作按钮
            when (app.status) {
                ApplicationStatus.PENDING -> {
                    if (showActions && currentTab == 0) {
                        // Received tab: 显示批准/拒绝按钮
                        holder.tvStatus.visibility = View.GONE
                        holder.layoutActions.visibility = View.VISIBLE
                        
                        holder.btnApprove.setOnClickListener {
                            onApprove(app)
                        }
                        
                        holder.btnReject.setOnClickListener {
                            onReject(app)
                        }
                    } else {
                        // Sent tab: 显示Pending状态
                        holder.tvStatus.visibility = View.VISIBLE
                        holder.layoutActions.visibility = View.GONE
                        holder.tvStatus.text = "Pending"
                        holder.tvStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
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

