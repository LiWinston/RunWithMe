package com.rwm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.rwm.dto.WorkoutData;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("workouts")
public class Workout {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")   // 外键，关联 users.id
    private Long userId;

    @TableField("workout_type") // 运动类型：OUTDOOR_RUN, TREADMILL, WALK
    private String workoutType;

    @TableField("distance") // 距离(km)
    private BigDecimal distance;

    @TableField("duration") // 持续时间(秒)
    private Integer duration;

    @TableField("steps") // 步数
    private Integer steps;

    @TableField("calories") // 卡路里
    private BigDecimal calories;

    @TableField("avg_speed") // 平均速度(km/h)
    private BigDecimal avgSpeed;

    @TableField("avg_pace") // 平均配速(秒/km)
    private Integer avgPace;

    @TableField("avg_heart_rate") // 平均心率(bpm)
    private Integer avgHeartRate;

    @TableField("max_heart_rate") // 最大心率(bpm)
    private Integer maxHeartRate;

    @TableField("start_time") // 开始时间
    private LocalDateTime startTime;

    @TableField("end_time") // 结束时间
    private LocalDateTime endTime;

    @TableField("status") // 运动状态：STARTED, PAUSED, COMPLETED, STOPPED
    private String status;

    @TableField("visibility") // 可见性：PUBLIC, GROUP, PRIVATE
    private String visibility;

    @TableField("goal_achieved") // 是否达成日目标
    private Boolean goalAchieved;

    @TableField("group_id") // 关联群组ID（可选）
    private Long groupId;

    @TableField("notes") // 运动备注
    private String notes;

    @TableField("weather_condition") // 天气条件
    private String weatherCondition;

    @TableField("temperature") // 温度(℃)
    private BigDecimal temperature;

    @TableField(value = "workout_data", typeHandler = JacksonTypeHandler.class) // 运动动态数据(JSON)
    private WorkoutData workoutData;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
