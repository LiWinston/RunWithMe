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
}
