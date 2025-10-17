package com.rwm.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 更新健身目标请求DTO
 */
@Data
public class UpdateFitnessGoalRequest {
    
    @Min(value = 0, message = "Weekly distance must be positive")
    private Double weeklyDistanceKm;
    
    @Min(value = 0, message = "Weekly workouts must be positive")
    private Integer weeklyWorkouts;
    
    @Min(value = 0, message = "Target pace must be positive")
    private Integer targetAvgPaceSecPerKm;
    
    @Min(value = 0, message = "Weekly calories must be positive")
    private Integer weeklyCalories;
}

