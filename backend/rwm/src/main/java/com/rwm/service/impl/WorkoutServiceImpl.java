package com.rwm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rwm.dto.request.WorkoutCreateRequest;
import com.rwm.dto.request.WorkoutUpdateRequest;
import com.rwm.dto.response.WorkoutStatsResponse;
import com.rwm.entity.Workout;
import com.rwm.entity.WorkoutRoute;
import com.rwm.mapper.WorkoutMapper;
import com.rwm.mapper.WorkoutRouteMapper;
import com.rwm.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 运动记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutServiceImpl implements WorkoutService {
    
    private final WorkoutMapper workoutMapper;
    private final WorkoutRouteMapper workoutRouteMapper;
    
    @Override
    @Transactional
    public Workout createWorkout(WorkoutCreateRequest request) {
        log.info("创建运动记录，用户ID: {}, 类型: {}", request.getUserId(), request.getWorkoutType());
        
        Workout workout = new Workout();
        BeanUtils.copyProperties(request, workout);
        
        // 设置默认值
        if (workout.getStartTime() == null) {
            workout.setStartTime(LocalDateTime.now());
        }
        
        // 计算目标达成状态
        workout.setGoalAchieved(calculateGoalAchievement(workout));
        
        int result = workoutMapper.insert(workout);
        if (result > 0) {
            log.info("运动记录创建成功，ID: {}", workout.getId());
            return workout;
        } else {
            throw new RuntimeException("创建运动记录失败");
        }
    }
    
    @Override
    @Transactional
    public Workout updateWorkout(Long workoutId, WorkoutUpdateRequest request) {
        log.info("更新运动记录，ID: {}", workoutId);
        
        Workout existingWorkout = getWorkoutById(workoutId);
        if (existingWorkout == null) {
            throw new RuntimeException("运动记录不存在");
        }
        
        // 只更新非null字段
        if (request.getDistance() != null) existingWorkout.setDistance(request.getDistance());
        if (request.getDuration() != null) existingWorkout.setDuration(request.getDuration());
        if (request.getSteps() != null) existingWorkout.setSteps(request.getSteps());
        if (request.getCalories() != null) existingWorkout.setCalories(request.getCalories());
        if (request.getAvgSpeed() != null) existingWorkout.setAvgSpeed(request.getAvgSpeed());
        if (request.getAvgPace() != null) existingWorkout.setAvgPace(request.getAvgPace());
        if (request.getAvgHeartRate() != null) existingWorkout.setAvgHeartRate(request.getAvgHeartRate());
        if (request.getMaxHeartRate() != null) existingWorkout.setMaxHeartRate(request.getMaxHeartRate());
        if (request.getEndTime() != null) existingWorkout.setEndTime(request.getEndTime());
        if (request.getStatus() != null) existingWorkout.setStatus(request.getStatus());
        if (request.getVisibility() != null) existingWorkout.setVisibility(request.getVisibility());
        if (request.getGoalAchieved() != null) existingWorkout.setGoalAchieved(request.getGoalAchieved());
        if (request.getGroupId() != null) existingWorkout.setGroupId(request.getGroupId());
        if (request.getNotes() != null) existingWorkout.setNotes(request.getNotes());
        if (request.getWeatherCondition() != null) existingWorkout.setWeatherCondition(request.getWeatherCondition());
        if (request.getTemperature() != null) existingWorkout.setTemperature(request.getTemperature());
        
        // 重新计算目标达成状态
        if (request.getGoalAchieved() == null) {
            existingWorkout.setGoalAchieved(calculateGoalAchievement(existingWorkout));
        }
        
        workoutMapper.updateById(existingWorkout);
        log.info("运动记录更新成功，ID: {}", workoutId);
        return existingWorkout;
    }
    
    @Override
    public Workout getWorkoutById(Long workoutId) {
        return workoutMapper.selectById(workoutId);
    }
    
    @Override
    @Transactional
    public void deleteWorkout(Long workoutId) {
        log.info("删除运动记录，ID: {}", workoutId);
        
        Workout workout = getWorkoutById(workoutId);
        if (workout == null) {
            throw new RuntimeException("运动记录不存在");
        }
        
        // 软删除
        workout.setDeleted(true);
        workoutMapper.updateById(workout);
        
        log.info("运动记录删除成功，ID: {}", workoutId);
    }
    
    @Override
    public List<Workout> getUserWorkouts(Long userId, int page, int size) {
        Page<Workout> pageParam = new Page<>(page, size);
        QueryWrapper<Workout> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("start_time");
        
        Page<Workout> result = workoutMapper.selectPage(pageParam, queryWrapper);
        return result.getRecords();
    }
    
    @Override
    public List<Workout> getUserWorkoutsByType(Long userId, String workoutType) {
        QueryWrapper<Workout> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("workout_type", workoutType)
                   .orderByDesc("start_time");
        
        return workoutMapper.selectList(queryWrapper);
    }
    
    @Override
    public boolean checkTodayGoalAchievement(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        
        QueryWrapper<Workout> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .between("start_time", startOfDay, endOfDay)
                   .eq("goal_achieved", true);
        
        List<Workout> todayWorkouts = workoutMapper.selectList(queryWrapper);
        return !todayWorkouts.isEmpty();
    }
    
    @Override
    public List<Workout> getUserWorkoutsByDate(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        QueryWrapper<Workout> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .between("start_time", startOfDay, endOfDay)
                   .orderByDesc("start_time");
        
        return workoutMapper.selectList(queryWrapper);
    }
    
    @Override
    public WorkoutStatsResponse getUserWorkoutStats(Long userId) {
        log.info("获取用户运动统计，用户ID: {}", userId);
        
        // 查询所有运动记录
        QueryWrapper<Workout> allQuery = new QueryWrapper<>();
        allQuery.eq("user_id", userId).eq("status", "COMPLETED");
        List<Workout> allWorkouts = workoutMapper.selectList(allQuery);
        
        // 查询本周记录
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        QueryWrapper<Workout> weekQuery = new QueryWrapper<>();
        weekQuery.eq("user_id", userId).ge("start_time", weekStart).eq("status", "COMPLETED");
        List<Workout> weekWorkouts = workoutMapper.selectList(weekQuery);
        
        // 查询本月记录
        LocalDateTime monthStart = LocalDate.now().minusDays(30).atStartOfDay();
        QueryWrapper<Workout> monthQuery = new QueryWrapper<>();
        monthQuery.eq("user_id", userId).ge("start_time", monthStart).eq("status", "COMPLETED");
        List<Workout> monthWorkouts = workoutMapper.selectList(monthQuery);
        
        WorkoutStatsResponse stats = new WorkoutStatsResponse();
        
        // 总体统计
        stats.setTotalWorkouts(allWorkouts.size());
        stats.setTotalDistance(allWorkouts.stream()
                .filter(w -> w.getDistance() != null)
                .map(Workout::getDistance)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        stats.setTotalDuration(allWorkouts.stream()
                .filter(w -> w.getDuration() != null)
                .mapToInt(Workout::getDuration)
                .sum());
        stats.setTotalCalories(allWorkouts.stream()
                .filter(w -> w.getCalories() != null)
                .map(Workout::getCalories)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        stats.setTotalSteps(allWorkouts.stream()
                .filter(w -> w.getSteps() != null)
                .mapToInt(Workout::getSteps)
                .sum());
        
        // 本周统计
        stats.setWeeklyWorkouts(weekWorkouts.size());
        stats.setWeeklyDistance(weekWorkouts.stream()
                .filter(w -> w.getDistance() != null)
                .map(Workout::getDistance)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // 本月统计
        stats.setMonthlyWorkouts(monthWorkouts.size());
        stats.setMonthlyDistance(monthWorkouts.stream()
                .filter(w -> w.getDistance() != null)
                .map(Workout::getDistance)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // 连续签到天数
        stats.setStreakDays(getUserStreakDays(userId));
        
        // 今日目标达成
        stats.setTodayGoalAchieved(checkTodayGoalAchievement(userId));
        
        return stats;
    }
    
    @Override
    @Transactional
    public Workout updateWorkoutStatus(Long workoutId, String status) {
        log.info("更新运动状态，ID: {}, 状态: {}", workoutId, status);
        
        Workout workout = getWorkoutById(workoutId);
        if (workout == null) {
            throw new RuntimeException("运动记录不存在");
        }
        
        workout.setStatus(status);
        
        // 如果是完成或停止状态，设置结束时间
        if ("COMPLETED".equals(status) || "STOPPED".equals(status)) {
            workout.setEndTime(LocalDateTime.now());
            // 重新计算目标达成状态
            workout.setGoalAchieved(calculateGoalAchievement(workout));
        }
        
        workoutMapper.updateById(workout);
        log.info("运动状态更新成功，ID: {}", workoutId);
        return workout;
    }
    
    @Override
    @Transactional
    public void saveWorkoutRoute(Long workoutId, List<WorkoutRoute> routes) {
        log.info("保存运动路线，workoutId: {}, 路点数量: {}", workoutId, routes.size());
        
        if (routes == null || routes.isEmpty()) {
            return;
        }
        
        // 设置workout_id和顺序
        for (int i = 0; i < routes.size(); i++) {
            WorkoutRoute route = routes.get(i);
            route.setWorkoutId(workoutId);
            route.setSequenceOrder(i + 1);
            workoutRouteMapper.insert(route);
        }
        
        log.info("运动路线保存成功，workoutId: {}", workoutId);
    }
    
    @Override
    public List<WorkoutRoute> getWorkoutRoute(Long workoutId) {
        return workoutRouteMapper.selectRouteByWorkoutId(workoutId);
    }
    
    @Override
    public boolean calculateGoalAchievement(Workout workout) {
        // 简化的目标判断逻辑：距离≥1km 或 时长≥15分钟
        boolean distanceGoal = workout.getDistance() != null && 
                              workout.getDistance().compareTo(new BigDecimal("1.0")) >= 0;
        boolean durationGoal = workout.getDuration() != null && 
                              workout.getDuration() >= 900; // 15分钟
        
        return distanceGoal || durationGoal;
    }
    
    @Override
    public int getUserStreakDays(Long userId) {
        int streak = 0;
        LocalDate currentDate = LocalDate.now();
        
        // 从今天开始往前查，直到找到第一个没有达成目标的日期
        while (true) {
            List<Workout> dayWorkouts = getUserWorkoutsByDate(userId, currentDate);
            boolean dayGoalAchieved = dayWorkouts.stream()
                    .anyMatch(workout -> Boolean.TRUE.equals(workout.getGoalAchieved()));
            
            if (dayGoalAchieved) {
                streak++;
                currentDate = currentDate.minusDays(1);
            } else {
                break;
            }
            
            // 避免无限循环，最多查询100天
            if (streak >= 100) {
                break;
            }
        }
        
        return streak;
    }
}
