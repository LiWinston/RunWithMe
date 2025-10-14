package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupFragment : Fragment() {

    // Mock data models
    data class GroupInfo(
        val id: String,          // Group ID
        val name: String,
        val week: Int,
        val score: Int,
        val weeklyProgress: Int,
        val weeklyGoal: Int,
        val waterDrops: Int,
        var progressScore: Int,  // 从数据库获取的进度分数 (0-100)
        var couponCount: Int,    // 获得的咖啡优惠券数量，TODO: 从数据库获取
        val memberCount: Int = 1 // 当前成员数量
    )

    data class Member(
        val userId: Long,
        val name: String,
        val completed: Boolean,
        val weeklyLikeCount: Int,
        val weeklyRemindCount: Int,
        val isSelf: Boolean
    )

    enum class ActionType {
        REMIND, LIKE
    }

    // Live data holders
    private var groupInfo: GroupInfo? = null
    private var hasGroup: Boolean = false
    
    // Activity result launchers
    private val createGroupLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val groupId = data?.getStringExtra("group_id")
            val groupName = data?.getStringExtra("group_name")
            
            // 创建group成功，更新UI
            if (groupId != null && groupName != null) {
                hasGroup = true
                groupInfo = GroupInfo(
                    id = groupId,
                    name = groupName,
                    week = 1,
                    score = 0,
                    weeklyProgress = 0,
                    weeklyGoal = 5,
                    waterDrops = 0,
                    progressScore = 0,
                    couponCount = 0,
                    memberCount = 1
                )
                view?.let { setupUI(it) }
            }
        }
    }
    
    private val qrCodeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val joinRequestedGroupId = data?.getStringExtra("join_requested_group_id")
            if (joinRequestedGroupId != null) {
                android.widget.Toast.makeText(
                    context,
                    "Join request sent!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化
        fetchMyGroupAndRender(view)
        setupUI(view)
    }
    
    private fun setupUI(view: View) {
        // 设置顶部导航栏
        setupTopBar(view)

        if (hasGroup && groupInfo != null) {
            // 有group，显示正常内容
            // 设置进度卡片
            setupProgressCard(view)

            // 设置成员列表
            setupMembersList(view)
        } else {
            // 没有group，显示创建提示
            showNoGroupView(view)
        }
    }

    private fun fetchMyGroupAndRender(root: View) {
        val api = com.example.myapplication.landr.RetrofitClient.create(com.example.myapplication.group.GroupApi::class.java)
        api.myGroup().enqueue(object: retrofit2.Callback<com.example.myapplication.group.Result<com.example.myapplication.group.GroupInfo>> {
            override fun onResponse(
                call: retrofit2.Call<com.example.myapplication.group.Result<com.example.myapplication.group.GroupInfo>>,
                response: retrofit2.Response<com.example.myapplication.group.Result<com.example.myapplication.group.GroupInfo>>
            ) {
                val res = response.body()
                if (response.isSuccessful && res != null && res.code == 0 && res.data != null) {
                    hasGroup = true
                    groupInfo = GroupInfo(
                        id = res.data.id.toString(),
                        name = res.data.name,
                        week = res.data.week ?: 0,
                        score = res.data.score ?: 0,
                        weeklyProgress = res.data.weeklyProgress ?: 0,
                        weeklyGoal = res.data.weeklyGoal ?: 0,
                        waterDrops = 0,
                        progressScore = res.data.weeklyProgress ?: 0,
                        couponCount = res.data.couponCount ?: 0,
                        memberCount = res.data.memberCount ?: 1
                    )
                } else {
                    hasGroup = false
                    groupInfo = null
                }
                setupUI(root)
            }

            override fun onFailure(
                call: retrofit2.Call<com.example.myapplication.group.Result<com.example.myapplication.group.GroupInfo>>,
                t: Throwable
            ) {
                hasGroup = false
                groupInfo = null
                setupUI(root)
            }
        })
    }

    private fun setupTopBar(view: View) {
        // 设置标题
        view.findViewById<TextView>(R.id.tv_group_name)?.text = groupInfo?.name ?: "Group"
        
        // Hamburger 菜单
        view.findViewById<ImageButton>(R.id.btn_menu)?.setOnClickListener { v ->
            showGroupMenu(v)
        }
    }
    
    private fun showNoGroupView(view: View) {
        // 隐藏进度卡片和成员列表
        view.findViewById<androidx.cardview.widget.CardView>(R.id.progress_card)?.visibility = View.GONE
        view.findViewById<androidx.cardview.widget.CardView>(R.id.members_card)?.visibility = View.GONE
        
        // TODO: 可以放置一个提示视图或按钮
    }

    private fun showGroupMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.group_menu, popup.menu)
        
        // 一个人只能有一个group
        // 没有group时：显示 Create Group 和 Join a Group
        // 有group时：显示 Invite Teammate, View Applications, Leave Group
        popup.menu.findItem(R.id.action_create_group)?.isVisible = !hasGroup
        popup.menu.findItem(R.id.action_scan_qrcode)?.isVisible = !hasGroup
        popup.menu.findItem(R.id.action_show_qrcode)?.isVisible = hasGroup
        popup.menu.findItem(R.id.action_view_applications)?.isVisible = hasGroup
        popup.menu.findItem(R.id.action_leave_group)?.isVisible = hasGroup
        
        // 检查group是否已满（最多6人）
        if (hasGroup) {
            val isGroupFull = groupInfo?.memberCount ?: 0 >= 6
            if (isGroupFull) {
                popup.menu.findItem(R.id.action_show_qrcode)?.isEnabled = false
            }
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_create_group -> {
                    openCreateGroup()
                    true
                }
                R.id.action_show_qrcode -> {
                    showGroupQRCode()
                    true
                }
                R.id.action_scan_qrcode -> {
                    scanQRCode()
                    true
                }
                R.id.action_view_applications -> {
                    viewApplications()
                    true
                }
                R.id.action_leave_group -> {
                    leaveGroup()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
    
    private fun openCreateGroup() {
        // 检查是否已有group
        if (hasGroup) {
            android.widget.Toast.makeText(
                context,
                "You already have a group. Leave it first to create a new one.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        val intent = Intent(requireContext(), CreateGroupActivity::class.java)
        createGroupLauncher.launch(intent)
    }
    
    private fun showGroupQRCode() {
        groupInfo?.let { info ->
            if (info.memberCount >= 6) {
                android.widget.Toast.makeText(
                    context,
                    "Group is full (max 6 members)",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return
            }
            
            val intent = Intent(requireContext(), QRCodeActivity::class.java)
            intent.putExtra(QRCodeActivity.EXTRA_MODE, QRCodeActivity.MODE_SHOW)
            intent.putExtra(QRCodeActivity.EXTRA_GROUP_ID, info.id)
            intent.putExtra(QRCodeActivity.EXTRA_GROUP_NAME, info.name)
            startActivity(intent)
        } ?: run {
            android.widget.Toast.makeText(
                context,
                "Please create a group first",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun scanQRCode() {
        // 检查是否已有group
        if (hasGroup) {
            android.widget.Toast.makeText(
                context,
                "You already have a group. Leave it first to join another.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        val intent = Intent(requireContext(), QRCodeActivity::class.java)
        intent.putExtra(QRCodeActivity.EXTRA_MODE, QRCodeActivity.MODE_SCAN)
        qrCodeLauncher.launch(intent)
    }
    
    private fun viewApplications() {
        val intent = Intent(requireContext(), GroupApplicationActivity::class.java)
        startActivity(intent)
    }
    
    private fun leaveGroup() {
        AlertDialog.Builder(requireContext())
            .setTitle("Leave Group")
            .setMessage("Are you sure you want to leave this group?")
            .setPositiveButton("Yes") { dialog, _ ->
                val api = com.example.myapplication.landr.RetrofitClient.create(com.example.myapplication.group.GroupApi::class.java)
                api.leave().enqueue(object: retrofit2.Callback<com.example.myapplication.group.Result<String>>{
                    override fun onResponse(
                        call: retrofit2.Call<com.example.myapplication.group.Result<String>>,
                        response: retrofit2.Response<com.example.myapplication.group.Result<String>>
                    ) {
                        hasGroup = false
                        groupInfo = null
                        view?.let { setupUI(it) }
                        android.widget.Toast.makeText(context, response.body()?.message ?: "Left group", android.widget.Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.example.myapplication.group.Result<String>>,
                        t: Throwable
                    ) {
                        android.widget.Toast.makeText(context, "Network error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                })
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupProgressCard(view: View) {
        val info = groupInfo ?: return
        
        // Week
        view.findViewById<TextView>(R.id.tv_week)?.text = "Week ${info.week}"

        // Score
        view.findViewById<TextView>(R.id.tv_score)?.text = info.score.toString()

        // 检查是否达到100，如果达到则清零并增加优惠券
        checkProgressMilestone()

    // Progress text - 显示 "Weekly Progress" + progressScore
    val progressText = "Weekly Progress ${info.progressScore}"
        view.findViewById<TextView>(R.id.tv_progress)?.text = progressText

        // Progress bar - 显示 progressScore/100 的比例
        view.findViewById<ProgressBar>(R.id.progress_bar)?.progress = info.progressScore

        // Coupon count - 显示优惠券数量
        view.findViewById<TextView>(R.id.tv_coupon_count)?.text = info.couponCount.toString()

        // Coffee image
        view.findViewById<ImageView>(R.id.iv_coffee)?.setImageResource(R.drawable.ic_launcher_foreground)
        try {
            val coffeeImageView = view.findViewById<ImageView>(R.id.iv_coffee)
            if (coffeeImageView != null) {
                coffeeImageView.setImageResource(R.drawable.coffee_cup)
                android.util.Log.d("GroupFragment", "Coffee image loaded successfully")
            } else {
                android.util.Log.e("GroupFragment", "iv_coffee ImageView not found")
            }
        } catch (e: Exception) {
            android.util.Log.e("GroupFragment", "Error loading coffee image", e)
            // 使用默认图片作为后备
            view.findViewById<ImageView>(R.id.iv_coffee)?.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Coffee progress is display-only; no interactive test logic
    }

    /**
     * 检查进度是否达到100
     * 如果达到100，则：
     * 1. 重置progressScore为0
     * 2. couponCount增加1
     * 3. 显示提示信息
     * 4. TODO: 保存到数据库
     */
    private fun checkProgressMilestone() {
        val info = groupInfo ?: return
        
        if (info.progressScore >= 100) {
            // 计算可以获得多少个优惠券（如果进度超过100）
            val couponsEarned = info.progressScore / 100
            val remainingProgress = info.progressScore % 100
            
            // 更新数据
            info.couponCount += couponsEarned
            info.progressScore = remainingProgress
            
            // TODO: 保存到数据库
            // saveToDatabase(groupInfo)
            
            // 显示恭喜对话框
            showCouponEarnedDialog(couponsEarned)
            
            android.util.Log.d("GroupFragment", 
                "Progress reached 100! Earned $couponsEarned coupon(s). " +
                "Total coupons: ${info.couponCount}, Remaining progress: ${info.progressScore}")
        }
    }
    
    /**
     * 显示获得优惠券的对话框
     */
    private fun showCouponEarnedDialog(couponsEarned: Int) {
        val info = groupInfo ?: return
        
        context?.let {
            AlertDialog.Builder(it)
                .setTitle("🎉 Congratulations!")
                .setMessage("Your group won $couponsEarned free coffee coupon${if (couponsEarned > 1) "s" else ""}! ☕\n\nTotal coupons: ${info.couponCount}")
                .setPositiveButton("Awesome, we'll keep going!") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }
    }
    
    /**
     * 模拟更新进度的函数（用于测试）
     * TODO: 实际应用中，这应该从数据库或API获取
     */
    private fun updateProgress(incrementBy: Int) {
        val info = groupInfo ?: return
        
        info.progressScore += incrementBy
        
        // 检查是否达到100
        if (info.progressScore >= 100) {
            checkProgressMilestone()
        }
        
        // 更新UI
        view?.let { v ->
            v.findViewById<TextView>(R.id.tv_progress)?.text = "Weekly Progress ${info.progressScore}"
            v.findViewById<ProgressBar>(R.id.progress_bar)?.progress = info.progressScore
            v.findViewById<TextView>(R.id.tv_coupon_count)?.text = info.couponCount.toString()
        }
        
        // TODO: 保存到数据库
        // saveToDatabase(groupInfo)
    }

    private fun setupMembersList(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.members_recycler)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        val api = com.example.myapplication.landr.RetrofitClient.create(com.example.myapplication.group.GroupApi::class.java)
        api.listMembers().enqueue(object: retrofit2.Callback<com.example.myapplication.group.Result<List<com.example.myapplication.group.GroupMemberInfo>>> {
            override fun onResponse(
                call: retrofit2.Call<com.example.myapplication.group.Result<List<com.example.myapplication.group.GroupMemberInfo>>>,
                response: retrofit2.Response<com.example.myapplication.group.Result<List<com.example.myapplication.group.GroupMemberInfo>>>
            ) {
                val res = response.body()
                val list = if (response.isSuccessful && res != null && res.code == 0 && res.data != null) res.data else emptyList()
                val mapped = list.map { info ->
                    Member(
                        userId = info.userId,
                        name = info.name,
                        completed = info.completedThisWeek,
                        weeklyLikeCount = info.weeklyLikeCount,
                        weeklyRemindCount = info.weeklyRemindCount,
                        isSelf = info.isSelf
                    )
                }
                recyclerView?.adapter = MembersAdapter(mapped) { member, action -> handleMemberAction(member, action) }
            }

            override fun onFailure(
                call: retrofit2.Call<com.example.myapplication.group.Result<List<com.example.myapplication.group.GroupMemberInfo>>>,
                t: Throwable
            ) {
                recyclerView?.adapter = MembersAdapter(emptyList()) { _, _ -> }
            }
        })
    }

    private fun handleMemberAction(member: Member, action: ActionType) {
        val (title, message) = when (action) {
            ActionType.REMIND -> Pair(
                "Remind ${member.name}",
                "Send a reminder to ${member.name} to go running?"
            )
            ActionType.LIKE -> Pair(
                "Like ${member.name}",
                "Like ${member.name}'s running progress?"
            )
        }
        
        // 显示确认弹框
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Send") { dialog, _ ->
                    val api = com.example.myapplication.landr.RetrofitClient.create(com.example.myapplication.group.GroupApi::class.java)
                    val body = com.example.myapplication.group.MemberInteractBody(targetUserId = member.userId, action = action.name)
                    api.interact(body).enqueue(object: retrofit2.Callback<com.example.myapplication.group.Result<String>>{
                        override fun onResponse(
                            call: retrofit2.Call<com.example.myapplication.group.Result<String>>,
                            response: retrofit2.Response<com.example.myapplication.group.Result<String>>
                        ) {
                            val msg = response.body()?.message ?: "Done"
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(
                            call: retrofit2.Call<com.example.myapplication.group.Result<String>>,
                            t: Throwable
                        ) {
                            android.widget.Toast.makeText(context, "Network error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    })
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    // RecyclerView Adapter
    inner class MembersAdapter(
        private val members: List<Member>,
        private val onActionClick: (Member, ActionType) -> Unit
    ) : RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

        inner class MemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val avatar: ImageView = view.findViewById(R.id.iv_avatar)
            val name: TextView = view.findViewById(R.id.tv_name)
            val stats: TextView = view.findViewById(R.id.tv_stats)
            val actionButton: LinearLayout = view.findViewById(R.id.btn_action)
            val actionIcon: ImageView = view.findViewById(R.id.iv_action_icon)
            val actionText: TextView = view.findViewById(R.id.tv_action_text)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_member, parent, false)
            return MemberViewHolder(view)
        }

        override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
            val member = members[position]

            holder.avatar.setImageResource(R.drawable.ic_profile)
            holder.name.text = member.name
            holder.stats.text = if (member.completed) "Completed this week" else "Not completed"

            // 决策显示交互按钮。完成周目标后可点赞，否则可督促；自己不可对自己操作
            val action = if (member.isSelf) null else if (member.completed) ActionType.LIKE else ActionType.REMIND
            if (action == null) {
                holder.actionButton.visibility = View.GONE
            } else {
                holder.actionButton.visibility = View.VISIBLE
                if (action == ActionType.LIKE) {
                    holder.actionIcon.setImageResource(R.drawable.group_like)
                    val likeText = if (member.weeklyLikeCount > 0) "Like ${member.weeklyLikeCount}" else "Like"
                    holder.actionText.text = likeText
                } else {
                    holder.actionIcon.setImageResource(R.drawable.group_remind)
                    val reminderText = if (member.weeklyRemindCount > 0) "Remind ${member.weeklyRemindCount}" else "Remind"
                    holder.actionText.text = reminderText
                }
                holder.actionButton.setBackgroundResource(R.drawable.btn_remind_like)
                holder.actionButton.setOnClickListener { onActionClick(member, action) }
            }

        }

        override fun getItemCount() = members.size
    }

    companion object {
        fun newInstance() = GroupFragment()
    }
}