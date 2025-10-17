package com.rwm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`notifications`")
public class Notification {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId; // recipient

    @TableField("actor_user_id")
    private Long actorUserId; // 触发者（如点赞/提醒的发起人）

    @TableField("target_user_id")
    private Long targetUserId; // 被操作的人（如被点赞/提醒的人）

    @TableField("group_id")
    private Long groupId; // 所属群组（若有）

    @TableField("type")
    private String type; // JOIN_REQUEST, JOIN_APPROVED, JOIN_REJECTED, LIKE, REMIND, GROUP_UPDATED

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("`read`")
    private Boolean read;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
