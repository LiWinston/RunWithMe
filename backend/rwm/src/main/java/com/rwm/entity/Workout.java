package com.rwm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("workouts")
public class Workout {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")   // 外键，关联 users.id
    private Long userId;

    @TableField("distance")
    private Double distance;

    @TableField("duration")
    private String duration;

    @TableField("calories")
    private Double calories;

    @TableField("speed")
    private Double speed;

    @TableField("date")
    private LocalDateTime date;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
