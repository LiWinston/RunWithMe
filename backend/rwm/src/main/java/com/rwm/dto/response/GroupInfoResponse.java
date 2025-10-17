package com.rwm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupInfoResponse {
    private Long id;
    private String name;
    private Integer week;
    private Integer score; // historical total points
    private Integer weeklyProgress; // 0-100
    private Integer weeklyGoal; // derived from members' average or fixed
    private Integer couponCount;
    private Integer memberCount;
    private Boolean isOwner;
}
