package com.rwm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notifications")
public class Notification {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId; // recipient

    @TableField("type")
    private String type; // JOIN_REQUEST, JOIN_APPROVED, JOIN_REJECTED, LIKE, REMIND, GROUP_UPDATED

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("read")
    private Boolean read;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
