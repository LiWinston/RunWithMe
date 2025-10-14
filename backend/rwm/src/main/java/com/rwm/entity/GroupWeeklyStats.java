package com.rwm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("group_weekly_stats")
public class GroupWeeklyStats {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("group_id")
    private Long groupId;

    @TableField("week_start")
    private LocalDate weekStart; // ISO week start (e.g., Monday UTC)

    @TableField("week_end")
    private LocalDate weekEnd; // end date inclusive

    @TableField("weekly_points")
    private Integer weeklyPoints; // progress 0-100 for coupon earning

    @TableField("total_points")
    private Integer totalPoints; // historical points sum

    @TableField("coupon_earned")
    private Integer couponEarned; // coupons earned this week

    @TableField("full_attendance_bonus_applied")
    private Boolean fullAttendanceBonusApplied; // 10pts bonus once per week

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
