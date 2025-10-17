package com.rwm.dto;

import lombok.Data;

/**
 * FitnessGoal JSON schema for User.fitnessGoal
 * Notes: stored as JSON via JacksonTypeHandler
 */
@Data
public class FitnessGoal {
    // core weekly running distance goal in km (used by social features)
    private Double weeklyDistanceKm;

    // optional: weekly workouts target count
    private Integer weeklyWorkouts;

    // optional: average pace target in seconds per km
    private Integer targetAvgPaceSecPerKm;

    // optional: calories target per week
    private Integer weeklyCalories;

    // extensible custom field bag if needed
    private Object extras;
}
