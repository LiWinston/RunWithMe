package com.rwm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("workout_routes")
public class WorkoutRoute {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("workout_id") // 外键，关联 workouts.id
    private Long workoutId;

    @TableField("latitude") // 纬度
    private BigDecimal latitude;

    @TableField("longitude") // 经度
    private BigDecimal longitude;

    @TableField("altitude") // 海拔(米)
    private BigDecimal altitude;

    @TableField("accuracy") // GPS精度(米)
    private BigDecimal accuracy;

    @TableField("speed") // 当前速度(km/h)
    private BigDecimal speed;

    @TableField("heart_rate") // 当前心率(bpm)
    private Integer heartRate;

    @TableField("timestamp") // 记录时间戳
    private LocalDateTime timestamp;

    @TableField("sequence_order") // 路线点顺序
    private Integer sequenceOrder;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
