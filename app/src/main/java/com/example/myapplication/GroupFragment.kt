package com.example.myapplication

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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupFragment : Fragment() {

    // Mock data models
    data class GroupInfo(
        val name: String,
        val week: Int,
        val score: Int,
        val weeklyProgress: Int,
        val weeklyGoal: Int,
        val waterDrops: Int
    )

    data class Member(
        val id: Int,
        val name: String,
        val avatarRes: Int,
        val distance: Double,
        val percentage: Int,
        val actionType: ActionType,
        val actionCount: Int = 0
    )

    enum class ActionType {
        REMIND, LIKE
    }

    // Mock data
    private lateinit var groupInfo: GroupInfo
    private lateinit var members: List<Member>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化 mock data
        initMockData()

        // 设置顶部导航栏
        setupTopBar(view)

        // 设置进度卡片
        setupProgressCard(view)

        // 设置成员列表
        setupMembersList(view)
    }

    private fun initMockData() {
        // Mock group info
        groupInfo = GroupInfo(
            name = "Group Name",
            week = 12,
            score = 120,
            weeklyProgress = 2,
            weeklyGoal = 5,
            waterDrops = 40
        )

        // Mock members
        members = listOf(
            Member(
                id = 1,
                name = "Siyu",
                avatarRes = R.drawable.ic_profile,
                distance = 8.9,
                percentage = 60,
                actionType = ActionType.REMIND
            ),
            Member(
                id = 2,
                name = "Michelle",
                avatarRes = R.drawable.ic_profile,
                distance = 18.0,
                percentage = 100,
                actionType = ActionType.LIKE,
                actionCount = 3
            ),
            Member(
                id = 3,
                name = "Yongchun",
                avatarRes = R.drawable.ic_profile,
                distance = 20.1,
                percentage = 100,
                actionType = ActionType.LIKE,
                actionCount = 4
            ),
            Member(
                id = 4,
                name = "Wenji",
                avatarRes = R.drawable.ic_profile,
                distance = 18.6,
                percentage = 90,
                actionType = ActionType.REMIND
            ),
            Member(
                id = 5,
                name = "Xiang",
                avatarRes = R.drawable.ic_profile,
                distance = 15.3,
                percentage = 85,
                actionType = ActionType.LIKE,
                actionCount = 2
            )
        )
    }

    private fun setupTopBar(view: View) {
        // 设置标题
        view.findViewById<TextView>(R.id.tv_group_name)?.text = groupInfo.name

        // 返回按钮
        view.findViewById<ImageButton>(R.id.btn_back)?.setOnClickListener {
            // 返回 Home 或上一个页面
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main, HomeFragment())
                .commit()
        }

        // Hamburger 菜单
        view.findViewById<ImageButton>(R.id.btn_menu)?.setOnClickListener { v ->
            showGroupMenu(v)
        }
    }

    private fun showGroupMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.group_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_leave_group -> {
                    // TODO: 处理退出小组
                    android.widget.Toast.makeText(
                        context,
                        "Leave Group",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    true
                }
                R.id.action_invite -> {
                    // TODO: 处理邀请队友
                    android.widget.Toast.makeText(
                        context,
                        "Invite Teammate",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setupProgressCard(view: View) {
        // Week
        view.findViewById<TextView>(R.id.tv_week)?.text = "Week ${groupInfo.week}"

        // Score
        view.findViewById<TextView>(R.id.tv_score)?.text = groupInfo.score.toString()

        // Progress text
        val progressText = "Weekly Progress ${groupInfo.weeklyProgress}/${groupInfo.weeklyGoal} · +${groupInfo.waterDrops}💧"
        view.findViewById<TextView>(R.id.tv_progress)?.text = progressText

        // Progress bar
        val progressPercentage = (groupInfo.weeklyProgress * 100) / groupInfo.weeklyGoal
        view.findViewById<ProgressBar>(R.id.progress_bar)?.progress = progressPercentage

        // Plant image
        view.findViewById<ImageView>(R.id.iv_plant)?.setImageResource(R.drawable.ic_launcher_foreground)
    }

    private fun setupMembersList(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.members_recycler)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = MembersAdapter(members) { member, action ->
            handleMemberAction(member, action)
        }
    }

    private fun handleMemberAction(member: Member, action: ActionType) {
        when (action) {
            ActionType.REMIND -> {
                android.widget.Toast.makeText(
                    context,
                    "Reminded ${member.name}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            ActionType.LIKE -> {
                android.widget.Toast.makeText(
                    context,
                    "Liked ${member.name}'s progress",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
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

            holder.avatar.setImageResource(member.avatarRes)
            holder.name.text = member.name
            holder.stats.text = "${member.distance}km / ${member.percentage}%"

            when (member.actionType) {
                ActionType.REMIND -> {
                    holder.actionIcon.setImageResource(R.drawable.group_remind)
                    holder.actionText.text = "Remind"
                    holder.actionButton.setBackgroundResource(R.drawable.btn_remind_like)
                }
                ActionType.LIKE -> {
                    holder.actionIcon.setImageResource(R.drawable.group_like)
                    val likeText = if (member.actionCount > 0) {
                        "Like ${member.actionCount}"
                    } else {
                        "Like"
                    }
                    holder.actionText.text = likeText
                    holder.actionButton.setBackgroundResource(R.drawable.btn_remind_like)
                }
            }

            holder.actionButton.setOnClickListener {
                onActionClick(member, member.actionType)
            }
        }

        override fun getItemCount() = members.size
    }

    companion object {
        fun newInstance() = GroupFragment()
    }
}