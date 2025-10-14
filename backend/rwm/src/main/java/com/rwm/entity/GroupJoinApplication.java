package com.rwm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("group_join_applications")
public class GroupJoinApplication {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("group_id")
    private Long groupId;

    @TableField("applicant_user_id")
    private Long applicantUserId;

    @TableField("inviter_user_id")
    private Long inviterUserId; // nullable, if invited by admin via QR

    @TableField("status")
    private String status; // PENDING, APPROVED, REJECTED

    @TableField("reason")
    private String reason; // optional message

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
