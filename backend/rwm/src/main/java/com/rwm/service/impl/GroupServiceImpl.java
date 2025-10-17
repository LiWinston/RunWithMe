package com.rwm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rwm.dto.request.*;
import com.rwm.dto.response.ApplicationItem;
import com.rwm.dto.response.GroupInfoResponse;
import com.rwm.entity.*;
import com.rwm.mapper.*;
import com.rwm.mapper.WorkoutMapper;
import com.rwm.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final GroupJoinApplicationMapper groupJoinApplicationMapper;
    private final GroupWeeklyStatsMapper groupWeeklyStatsMapper;
    private final UserWeeklyContributionMapper userWeeklyContributionMapper;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final WorkoutMapper workoutMapper;

    private LocalDate weekStartUtc(LocalDate date) {
        // Assume Monday as start
        return date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
    }

    private LocalDate weekEndUtc(LocalDate date) {
        return weekStartUtc(date).plusDays(6);
    }

    @Override
    @Transactional
    public Group createGroup(Long ownerId, GroupCreateRequest request) {
        // ensure user not in any group
        if (getUserCurrentGroupId(ownerId) != null) {
            throw new RuntimeException("User already in a group");
        }

        Group group = new Group();
        group.setName(request.getName());
        group.setOwnerId(ownerId);
        group.setMemberLimit(request.getMemberLimit() == null ? 6 : request.getMemberLimit());
        group.setCouponCount(0);
        group.setDeleted(false);
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        groupMapper.insert(group);

        GroupMember gm = new GroupMember();
        gm.setGroupId(group.getId());
        gm.setUserId(ownerId);
        gm.setRole("ADMIN");
        gm.setJoinedAt(LocalDateTime.now());
        gm.setWeeklyLikeCount(0);
        gm.setWeeklyRemindCount(0);
        gm.setDeleted(false);
        groupMemberMapper.insert(gm);

        // init weekly stats
        GroupWeeklyStats stats = new GroupWeeklyStats();
        stats.setGroupId(group.getId());
        stats.setWeekStart(weekStartUtc(LocalDate.now()));
        stats.setWeekEnd(weekEndUtc(LocalDate.now()));
        stats.setWeeklyPoints(0);
        stats.setTotalPoints(0);
        stats.setCouponEarned(0);
        stats.setFullAttendanceBonusApplied(false);
        groupWeeklyStatsMapper.insert(stats);

        return group;
    }

    private Long getUserCurrentGroupId(Long userId) {
        QueryWrapper<GroupMember> q = new QueryWrapper<>();
        q.eq("user_id", userId).eq("deleted", false);
        GroupMember gm = groupMemberMapper.selectOne(q);
        return gm == null ? null : gm.getGroupId();
    }

    @Override
    @Transactional
    public void leaveGroup(Long userId) {
        QueryWrapper<GroupMember> q = new QueryWrapper<>();
        q.eq("user_id", userId).eq("deleted", false);
        GroupMember gm = groupMemberMapper.selectOne(q);
        if (gm == null) return;

        Group group = groupMapper.selectById(gm.getGroupId());
        boolean isOwner = Objects.equals(group.getOwnerId(), userId);

        // soft delete membership
        gm.setDeleted(true);
        groupMemberMapper.updateById(gm);

        if (isOwner) {
            transferOwnershipIfOwnerLeaves(group.getId());
        }
    }

    @Override
    @Transactional
    public void transferOwnershipIfOwnerLeaves(Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) return;

        // pick earliest join member
        QueryWrapper<GroupMember> q = new QueryWrapper<>();
        q.eq("group_id", groupId).eq("deleted", false).orderByAsc("joined_at");
        List<GroupMember> members = groupMemberMapper.selectList(q);
        if (members.isEmpty()) {
            // no members left
            return;
        }
        GroupMember next = members.get(0);
        group.setOwnerId(next.getUserId());
        groupMapper.updateById(group);

        // update role
        next.setRole("ADMIN");
        groupMemberMapper.updateById(next);
    }

    @Override
    public GroupInfoResponse getMyGroupInfo(Long userId) {
        Long gid = getUserCurrentGroupId(userId);
        if (gid == null) return null;
        Group g = groupMapper.selectById(gid);
        boolean isOwner = Objects.equals(g.getOwnerId(), userId);

        // member count
        QueryWrapper<GroupMember> q = new QueryWrapper<>();
        q.eq("group_id", gid).eq("deleted", false);
        int memberCount = groupMemberMapper.selectCount(q).intValue();

        // weekly stats
        LocalDate ws = weekStartUtc(LocalDate.now());
        QueryWrapper<GroupWeeklyStats> wq = new QueryWrapper<>();
        wq.eq("group_id", gid).eq("week_start", ws);
        GroupWeeklyStats stats = groupWeeklyStatsMapper.selectOne(wq);
        if (stats == null) {
            stats = new GroupWeeklyStats();
            stats.setGroupId(gid);
            stats.setWeekStart(ws);
            stats.setWeekEnd(weekEndUtc(ws));
            stats.setWeeklyPoints(0);
            stats.setTotalPoints(0);
            stats.setCouponEarned(0);
            stats.setFullAttendanceBonusApplied(false);
            groupWeeklyStatsMapper.insert(stats);
        }

        return new GroupInfoResponse(
                g.getId(), g.getName(), 0,
                stats.getTotalPoints() == null ? 0 : stats.getTotalPoints(),
                stats.getWeeklyPoints() == null ? 0 : stats.getWeeklyPoints(),
                5,
                g.getCouponCount() == null ? 0 : g.getCouponCount(),
                memberCount,
                isOwner
        );
    }

    @Override
    @Transactional
    public GroupJoinApplication requestJoin(Long userId, GroupJoinRequest request) {
        // ensure user not in group
        if (getUserCurrentGroupId(userId) != null) {
            throw new RuntimeException("User already in a group");
        }

        Group g = groupMapper.selectById(request.getGroupId());
        if (g == null) throw new RuntimeException("Group not found");

        // capacity check
        QueryWrapper<GroupMember> qCount = new QueryWrapper<>();
        qCount.eq("group_id", g.getId()).eq("deleted", false);
        int count = groupMemberMapper.selectCount(qCount).intValue();
        if (count >= g.getMemberLimit()) throw new RuntimeException("Group is full");

        // if inviter is owner -> direct join
        if (request.getInviterUserId() != null && Objects.equals(request.getInviterUserId(), g.getOwnerId())) {
            GroupMember gm = new GroupMember();
            gm.setGroupId(g.getId());
            gm.setUserId(userId);
            gm.setRole("MEMBER");
            gm.setJoinedAt(LocalDateTime.now());
            gm.setWeeklyLikeCount(0);
            gm.setWeeklyRemindCount(0);
            gm.setDeleted(false);
            groupMemberMapper.insert(gm);
            // notify owner
            sendNotification(g.getOwnerId(), "JOIN_APPROVED", "Member joined", "A user joined via your QR code");
            return null;
        }

        // else create application pending
        GroupJoinApplication app = new GroupJoinApplication();
        app.setGroupId(g.getId());
        app.setApplicantUserId(userId);
        app.setInviterUserId(request.getInviterUserId());
        app.setStatus("PENDING");
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        app.setDeleted(false);
        groupJoinApplicationMapper.insert(app);

        // notify owner
        sendNotification(g.getOwnerId(), "JOIN_REQUEST", "Join request", "A user requested to join your group");

        return app;
    }

    @Override
    public List<ApplicationItem> getReceivedApplications(Long ownerId) {
        // find groups owned by owner
        QueryWrapper<Group> gq = new QueryWrapper<>();
        gq.eq("owner_id", ownerId);
        List<Group> owned = groupMapper.selectList(gq);
        if (owned.isEmpty()) return List.of();
        List<Long> groupIds = owned.stream().map(Group::getId).toList();

        QueryWrapper<GroupJoinApplication> aq = new QueryWrapper<>();
        aq.in("group_id", groupIds).orderByDesc("created_at");
        List<GroupJoinApplication> apps = groupJoinApplicationMapper.selectList(aq);

        return apps.stream().map(a -> new ApplicationItem(
                a.getId(),
                a.getApplicantUserId(),
                userSafeName(a.getApplicantUserId()),
                a.getGroupId(),
                groupName(a.getGroupId()),
                a.getCreatedAt() == null ? System.currentTimeMillis() : a.getCreatedAt().atZone(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli(),
                a.getStatus()
        )).collect(Collectors.toList());
    }

    private String userSafeName(Long uid) {
        var u = userMapper.selectById(uid);
        if (u == null) return "User";
        return (u.getFirstName() != null ? u.getFirstName() : u.getUsername());
    }

    private String groupName(Long gid) {
        var g = groupMapper.selectById(gid);
        return g == null ? "Group" : g.getName();
    }

    @Override
    @Transactional
    public void moderateApplication(Long ownerId, Long applicationId, boolean approve, String reason) {
        GroupJoinApplication app = groupJoinApplicationMapper.selectById(applicationId);
        if (app == null) throw new RuntimeException("Application not found");
        Group group = groupMapper.selectById(app.getGroupId());
        if (group == null || !Objects.equals(group.getOwnerId(), ownerId)) {
            throw new RuntimeException("No permission");
        }

        if (!"PENDING".equals(app.getStatus())) return;

        if (approve) {
            // capacity check
            QueryWrapper<GroupMember> qCount = new QueryWrapper<>();
            qCount.eq("group_id", group.getId()).eq("deleted", false);
            int count = groupMemberMapper.selectCount(qCount).intValue();
            if (count >= group.getMemberLimit()) throw new RuntimeException("Group is full");

            GroupMember gm = new GroupMember();
            gm.setGroupId(group.getId());
            gm.setUserId(app.getApplicantUserId());
            gm.setRole("MEMBER");
            gm.setJoinedAt(LocalDateTime.now());
            gm.setWeeklyLikeCount(0);
            gm.setWeeklyRemindCount(0);
            gm.setDeleted(false);
            groupMemberMapper.insert(gm);

            app.setStatus("APPROVED");
            app.setReason(null);
            app.setUpdatedAt(LocalDateTime.now());
            groupJoinApplicationMapper.updateById(app);

            sendNotification(app.getApplicantUserId(), "JOIN_APPROVED", "Join approved", "Your join request was approved");
        } else {
            app.setStatus("REJECTED");
            app.setReason(reason);
            app.setUpdatedAt(LocalDateTime.now());
            groupJoinApplicationMapper.updateById(app);
            sendNotification(app.getApplicantUserId(), "JOIN_REJECTED", "Join rejected", reason == null ? "Your join request was rejected" : reason);
        }
    }

    @Override
    @Transactional
    public void likeOrRemind(Long actorUserId, Long targetUserId, String action) {
        if (Objects.equals(actorUserId, targetUserId)) {
            throw new RuntimeException("Cannot operate on yourself");
        }
        Long actorGroupId = getUserCurrentGroupId(actorUserId);
        Long targetGroupId = getUserCurrentGroupId(targetUserId);
        if (actorGroupId == null || !Objects.equals(actorGroupId, targetGroupId)) {
            throw new RuntimeException("Not in same group");
        }
        // update weekly counters (weekly unique per actor-target-action)
        QueryWrapper<GroupMember> qActor = new QueryWrapper<>();
        qActor.eq("user_id", targetUserId).eq("group_id", actorGroupId).eq("deleted", false);
        GroupMember target = groupMemberMapper.selectOne(qActor);
        if (target == null) throw new RuntimeException("Target not found");

        LocalDate ws = weekStartUtc(LocalDate.now());
        String actionUpper = action.toUpperCase();
        if (!"LIKE".equals(actionUpper) && !"REMIND".equals(actionUpper)) {
            throw new RuntimeException("Invalid action");
        }

        // 检查当周是否已计数（同一 actor -> target 的同一动作）
        QueryWrapper<Notification> nq = new QueryWrapper<>();
        nq.eq("actor_user_id", actorUserId)
          .eq("target_user_id", targetUserId)
          .eq("type", actionUpper)
          .ge("created_at", ws.atStartOfDay());
    Long cnt = notificationMapper.selectCount(nq);
    boolean alreadyCountedThisWeek = cnt != null && cnt > 0;

        if (!alreadyCountedThisWeek) {
            if ("LIKE".equals(actionUpper)) {
                target.setWeeklyLikeCount((target.getWeeklyLikeCount() == null ? 0 : target.getWeeklyLikeCount()) + 1);
            } else {
                target.setWeeklyRemindCount((target.getWeeklyRemindCount() == null ? 0 : target.getWeeklyRemindCount()) + 1);
            }
            groupMemberMapper.updateById(target);
        }

        // 始终记录通知事件（用于审计/时间线）
        sendNotificationDetailed(targetUserId, actorUserId, targetUserId, actorGroupId, actionUpper,
                "LIKE".equals(actionUpper) ? "New like" : "Reminder",
                "LIKE".equals(actionUpper) ? "Someone liked your progress" : "Please keep up with your plan");
    }

    private void sendNotification(Long userId, String type, String title, String content) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(n);
    }

    private void sendNotificationDetailed(Long recipientUserId, Long actorUserId, Long targetUserId, Long groupId,
                                          String type, String title, String content) {
        Notification n = new Notification();
        n.setUserId(recipientUserId);
        n.setActorUserId(actorUserId);
        n.setTargetUserId(targetUserId);
        n.setGroupId(groupId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(n);
    }

    @Override
    @Transactional
    public void completeWeeklyPlan(Long userId) {
        Long gid = getUserCurrentGroupId(userId);
        if (gid == null) throw new RuntimeException("User not in a group");
        LocalDate ws = weekStartUtc(LocalDate.now());
        LocalDate we = weekEndUtc(LocalDate.now());

        // ensure one-group-per-week contribution
        QueryWrapper<UserWeeklyContribution> uq = new QueryWrapper<>();
        uq.eq("user_id", userId).eq("week_start", ws);
        UserWeeklyContribution c = userWeeklyContributionMapper.selectOne(uq);
        if (c != null && !Objects.equals(c.getGroupId(), gid)) {
            throw new RuntimeException("Weekly contribution already made for another group");
        }

        if (c == null) {
            c = new UserWeeklyContribution();
            c.setUserId(userId);
            c.setGroupId(gid);
            c.setWeekStart(ws);
            c.setWeekEnd(we);
            c.setIndividualCompleted(true);
            userWeeklyContributionMapper.insert(c);
        } else if (Boolean.TRUE.equals(c.getIndividualCompleted())) {
            // already contributed
            return;
        } else {
            c.setIndividualCompleted(true);
            userWeeklyContributionMapper.updateById(c);
        }

        // +15 points to group weekly
        QueryWrapper<GroupWeeklyStats> wq = new QueryWrapper<>();
        wq.eq("group_id", gid).eq("week_start", ws);
        GroupWeeklyStats stats = groupWeeklyStatsMapper.selectOne(wq);
        if (stats == null) {
            stats = new GroupWeeklyStats();
            stats.setGroupId(gid);
            stats.setWeekStart(ws);
            stats.setWeekEnd(we);
            stats.setWeeklyPoints(0);
            stats.setTotalPoints(0);
            stats.setCouponEarned(0);
            stats.setFullAttendanceBonusApplied(false);
            groupWeeklyStatsMapper.insert(stats);
        }
        int newWeekly = (stats.getWeeklyPoints() == null ? 0 : stats.getWeeklyPoints()) + 15;
        stats.setWeeklyPoints(newWeekly);

        // full attendance detection: all current members have individualCompleted=true in this week
        if (!Boolean.TRUE.equals(stats.getFullAttendanceBonusApplied())) {
            QueryWrapper<GroupMember> mq = new QueryWrapper<>();
            mq.eq("group_id", gid).eq("deleted", false);
            List<GroupMember> members = groupMemberMapper.selectList(mq);
            int completed = 0;
            for (GroupMember m : members) {
                QueryWrapper<UserWeeklyContribution> mq2 = new QueryWrapper<>();
                mq2.eq("user_id", m.getUserId()).eq("week_start", ws);
                UserWeeklyContribution mc = userWeeklyContributionMapper.selectOne(mq2);
                if (mc != null && Boolean.TRUE.equals(mc.getIndividualCompleted())) {
                    completed++;
                }
            }
            if (completed == members.size() && members.size() > 0) {
                stats.setWeeklyPoints(stats.getWeeklyPoints() + 10);
                stats.setFullAttendanceBonusApplied(true);
            }
        }

        // award coupons for every 100 weekly points, carry remainder
        int couponsEarned = stats.getWeeklyPoints() / 100;
        int remainder = stats.getWeeklyPoints() % 100;
        if (couponsEarned > 0) {
            stats.setCouponEarned((stats.getCouponEarned() == null ? 0 : stats.getCouponEarned()) + couponsEarned);
            stats.setWeeklyPoints(remainder);
            Group g = groupMapper.selectById(gid);
            g.setCouponCount((g.getCouponCount() == null ? 0 : g.getCouponCount()) + couponsEarned);
            groupMapper.updateById(g);
            sendNotification(g.getOwnerId(), "GROUP_UPDATED", "Coupon earned", "Your group earned " + couponsEarned + " coupon(s)");
        }

        stats.setTotalPoints((stats.getTotalPoints() == null ? 0 : stats.getTotalPoints()) + 15);
        groupWeeklyStatsMapper.updateById(stats);
    }

    @Override
    public List<com.rwm.dto.response.GroupMemberInfo> listMembers(Long userId) {
        Long gid = getUserCurrentGroupId(userId);
        if (gid == null) return List.of();
        LocalDate ws = weekStartUtc(LocalDate.now());
        QueryWrapper<GroupMember> mq = new QueryWrapper<>();
        mq.eq("group_id", gid).eq("deleted", false).orderByAsc("joined_at");
        List<GroupMember> members = groupMemberMapper.selectList(mq);
        return members.stream().map(m -> {
            String name = userSafeName(m.getUserId());
            // check weekly completion
            QueryWrapper<UserWeeklyContribution> uq = new QueryWrapper<>();
            uq.eq("user_id", m.getUserId()).eq("week_start", ws);
            UserWeeklyContribution c = userWeeklyContributionMapper.selectOne(uq);
            boolean completed = c != null && Boolean.TRUE.equals(c.getIndividualCompleted());

            // aggregate this week's distance from workouts
            java.math.BigDecimal weekDistance = java.math.BigDecimal.ZERO;
            try {
                // Sum workouts distance for this user since week start, completed only
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.rwm.entity.Workout> wq = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                wq.eq("user_id", m.getUserId())
                  .ge("start_time", ws.atStartOfDay())
                  .eq("status", "COMPLETED");
                List<com.rwm.entity.Workout> workouts = workoutMapper.selectList(wq);
                for (com.rwm.entity.Workout w : workouts) {
                    if (w.getDistance() != null) weekDistance = weekDistance.add(w.getDistance());
                }
            } catch (Exception ex) {
                log.warn("Failed to sum weekly distance for user {}: {}", m.getUserId(), ex.getMessage());
            }

            // fetch user's weekly goal from profile
            Double goal = null;
            var user = userMapper.selectById(m.getUserId());
            if (user != null && user.getFitnessGoal() != null && user.getFitnessGoal().getWeeklyDistanceKm() != null) {
                goal = user.getFitnessGoal().getWeeklyDistanceKm();
            }
            double done = weekDistance.doubleValue();
            int percent = 0;
            if (goal != null && goal > 0) {
                percent = (int) Math.min(100, Math.round((done / goal) * 100));
            }
            return new com.rwm.dto.response.GroupMemberInfo(
                    m.getUserId(),
                    name,
                    m.getWeeklyLikeCount() == null ? 0 : m.getWeeklyLikeCount(),
                    m.getWeeklyRemindCount() == null ? 0 : m.getWeeklyRemindCount(),
                    completed,
                    Objects.equals(m.getUserId(), userId),
                    done,
                    goal,
                    percent
            );
        }).collect(Collectors.toList());
    }

    @Override
    public List<Notification> myNotifications(Long userId, int limit) {
        QueryWrapper<Notification> nq = new QueryWrapper<>();
        nq.eq("user_id", userId).orderByDesc("created_at").last("LIMIT " + Math.max(1, limit));
        return notificationMapper.selectList(nq);
    }

    @Override
    public com.rwm.dto.response.FeedResponse feed(Long userId, int limit) {
        Long gid = getUserCurrentGroupId(userId);
        int lim = Math.max(1, limit);
        java.util.List<com.rwm.entity.Workout> workouts;
        java.util.List<Notification> interactions = java.util.List.of();

        if (gid == null) {
            // 无组：仅返回自己的 workouts
            QueryWrapper<com.rwm.entity.Workout> wq = new QueryWrapper<>();
            wq.eq("user_id", userId)
              .eq("status", "COMPLETED")
              .orderByDesc("start_time")
              .last("LIMIT " + lim);
            workouts = workoutMapper.selectList(wq);
        } else {
            // 有组：返回组员 workouts
            java.util.Set<Long> memberIds = new java.util.HashSet<>();
            QueryWrapper<GroupMember> mq = new QueryWrapper<>();
            mq.eq("group_id", gid).eq("deleted", false);
            for (GroupMember m : groupMemberMapper.selectList(mq)) memberIds.add(m.getUserId());

            if (memberIds.isEmpty()) {
                workouts = java.util.List.of();
            } else {
                QueryWrapper<com.rwm.entity.Workout> wq = new QueryWrapper<>();
                wq.in("user_id", memberIds)
                  .eq("status", "COMPLETED")
                  .orderByDesc("start_time")
                  .last("LIMIT " + lim);
                workouts = workoutMapper.selectList(wq);
            }

            // 组内的互动记录（LIKE/REMIND），基于通知（包含 group_id/actor/target）
            QueryWrapper<Notification> iq = new QueryWrapper<>();
            iq.eq("group_id", gid)
              .in("type", java.util.List.of("LIKE", "REMIND"))
              .orderByDesc("created_at")
              .last("LIMIT " + lim);
            interactions = notificationMapper.selectList(iq);
        }

        return new com.rwm.dto.response.FeedResponse(workouts, interactions);
    }
}
