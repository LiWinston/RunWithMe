package com.rwm.service;

import com.rwm.dto.request.*;
import com.rwm.dto.response.ApplicationItem;
import com.rwm.dto.response.GroupInfoResponse;
import com.rwm.entity.Group;
import com.rwm.entity.GroupJoinApplication;

import java.util.List;

public interface GroupService {
    Group createGroup(Long ownerId, GroupCreateRequest request);
    void leaveGroup(Long userId);
    void transferOwnershipIfOwnerLeaves(Long groupId);
    GroupInfoResponse getMyGroupInfo(Long userId);
    GroupJoinApplication requestJoin(Long userId, GroupJoinRequest request);
    List<ApplicationItem> getReceivedApplications(Long ownerId);
    void moderateApplication(Long ownerId, Long applicationId, boolean approve, String reason);
    void likeOrRemind(Long actorUserId, Long targetUserId, String action);

    // mark an user weekly plan completed to contribute to group score
    void completeWeeklyPlan(Long userId);

    java.util.List<com.rwm.dto.response.GroupMemberInfo> listMembers(Long userId);
    java.util.List<com.rwm.entity.Notification> myNotifications(Long userId, int limit);

    /**
     * 动态审计：
     * - 有组：返回本组成员的 workouts 与组内点赞/提醒事件
     * - 无组：返回当前用户的 workouts
     */
    com.rwm.dto.response.FeedResponse feed(Long userId, int limit);
}
