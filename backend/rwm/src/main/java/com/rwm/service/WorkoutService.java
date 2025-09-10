package com.rwm.service;

import com.rwm.entity.Workout;
import com.rwm.dto.request.WorkoutCreateRequest;
import com.rwm.dto.request.WorkoutUpdateRequest;
import com.rwm.dto.response.WorkoutStatsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 运动记录服务接口
 */
public interface WorkoutService {
    
    /**
     * 创建运动记录
     */
    Workout createWorkout(WorkoutCreateRequest request);
    
    /**
     * 更新运动记录
     */
    Workout updateWorkout(Long workoutId, WorkoutUpdateRequest request);
    
    /**
     * 根据ID获取运动记录
     */
    Workout getWorkoutById(Long workoutId);
    
    /**
     * 删除运动记录
     */
    void deleteWorkout(Long workoutId);
    
    /**
     * 获取用户运动记录列表
     */
    List<Workout> getUserWorkouts(Long userId, int page, int size);
    
    /**
     * 按运动类型获取用户记录
     */
    List<Workout> getUserWorkoutsByType(Long userId, String workoutType);
    
    /**
     * 获取用户今日目标达成状态
     */
    boolean checkTodayGoalAchievement(Long userId);
    
    /**
     * 获取用户指定日期的运动记录
     */
    List<Workout> getUserWorkoutsByDate(Long userId, LocalDate date);
    
    /**
     * 获取用户运动统计数据
     */
    WorkoutStatsResponse getUserWorkoutStats(Long userId);
    
    /**
     * 更新运动状态
     */
    Workout updateWorkoutStatus(Long workoutId, String status);
    
    /**
     * 获取用户今日运动统计（总距离、总时长、总卡路里、运动次数）
     */
    Map<String, Object> getUserTodayStats(Long userId);
    
    /**
     * 获取用户本周运动统计
     */
    Map<String, Object> getUserWeekStats(Long userId);
    
    /**
     * 获取用户本月运动统计
     */
    Map<String, Object> getUserMonthStats(Long userId);
    
    /**
     * 获取用户指定时间范围的运动记录（用于History展示）
     */
    List<Workout> getUserWorkoutsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户本周每日运动数据（用于周图表）
     */
    Map<String, Object> getUserWeeklyChart(Long userId);
    
    /**
     * 获取用户本月运动趋势（用于月图表）
     */
    Map<String, Object> getUserMonthlyChart(Long userId);
    
    /**
     * 计算运动目标达成状态
     */
    boolean calculateGoalAchievement(Workout workout);
    
    /**
     * 获取用户连续签到天数
     */
    int getUserStreakDays(Long userId);
}
