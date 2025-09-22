package com.rwm.dto.request;

import com.rwm.dto.WorkoutData;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建运动记录请求DTO
 */
@Data
public class WorkoutCreateRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotBlank(message = "运动类型不能为空")
    @Pattern(regexp = "OUTDOOR_RUN|TREADMILL|WALK|CYCLING|SWIMMING|OTHER", message = "运动类型无效")
    private String workoutType;
    
    @DecimalMin(value = "0.0", message = "距离不能为负数")
    private BigDecimal distance;
    
    @Min(value = 0, message = "持续时间不能为负数")
    private Integer duration;
    
    @Min(value = 0, message = "步数不能为负数")
    private Integer steps;
    
    @DecimalMin(value = "0.0", message = "卡路里不能为负数")
    private BigDecimal calories;
    
    @DecimalMin(value = "0.0", message = "平均速度不能为负数")
    private BigDecimal avgSpeed;
    
    @Min(value = 0, message = "平均配速不能为负数")
    private Integer avgPace;
    
    @Min(value = 0, message = "平均心率不能为负数")
    @Max(value = 250, message = "平均心率超出正常范围")
    private Integer avgHeartRate;
    
    @Min(value = 0, message = "最大心率不能为负数")
    @Max(value = 250, message = "最大心率超出正常范围")
    private Integer maxHeartRate;
    
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @Pattern(regexp = "STARTED|PAUSED|COMPLETED|STOPPED", message = "运动状态无效")
    private String status = "STARTED";
    
    @Pattern(regexp = "PUBLIC|GROUP|PRIVATE", message = "可见性设置无效")
    private String visibility = "PRIVATE";
    
    private Boolean goalAchieved = false;
    
    private Long groupId;
    
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String notes;
    
    @Size(max = 50, message = "天气条件长度不能超过50字符")
    private String weatherCondition;
    
    private BigDecimal temperature;
    
    // JSON动态数据：路线轨迹、速度样本、心率样本等
    private WorkoutData workoutData;
}
