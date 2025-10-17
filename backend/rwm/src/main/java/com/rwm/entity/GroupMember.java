package com.rwm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`group_members`")
public class GroupMember {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("group_id")
    private Long groupId;

    @TableField("user_id")
    private Long userId;

    @TableField("role")
    private String role; // ADMIN or MEMBER

    @TableField("joined_at")
    private LocalDateTime joinedAt;

    @TableField("weekly_like_count")
    private Integer weeklyLikeCount;

    @TableField("weekly_remind_count")
    private Integer weeklyRemindCount;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
