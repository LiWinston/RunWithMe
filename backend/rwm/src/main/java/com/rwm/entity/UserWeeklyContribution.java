package com.rwm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("`user_weekly_contributions`")
public class UserWeeklyContribution {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("group_id")
    private Long groupId;

    @TableField("week_start")
    private LocalDate weekStart;

    @TableField("week_end")
    private LocalDate weekEnd;

    @TableField("individual_completed")
    private Boolean individualCompleted; // has contributed +15 once

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
