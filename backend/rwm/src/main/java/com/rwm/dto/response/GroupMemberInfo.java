package com.rwm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberInfo {
    private Long userId;
    private String name;
    private Integer weeklyLikeCount;
    private Integer weeklyRemindCount;
    private Boolean completedThisWeek; // derived from user_weekly_contributions
    private Boolean isSelf;

    // Extended fields for UI progress: this week's distance and goal
    private Double weeklyDistanceKmDone; // 已跑公里数（本周）
    private Double weeklyDistanceKmGoal; // 个人周目标公里数
    private Integer progressPercent;     // 进度百分比（0-100）
}
