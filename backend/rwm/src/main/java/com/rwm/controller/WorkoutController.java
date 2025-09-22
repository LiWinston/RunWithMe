package com.rwm.controller;

import com.rwm.dto.request.WorkoutCreateRequest;
import com.rwm.dto.request.WorkoutUpdateRequest;
import com.rwm.dto.response.Result;
import com.rwm.dto.response.WorkoutStatsResponse;
import com.rwm.entity.Workout;
import com.rwm.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 运动记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@Validated
public class WorkoutController {

    private final WorkoutService workoutService;

    /**
     * 创建运动记录
     */
    @PostMapping
    public ResponseEntity<Result<Workout>> createWorkout(@Valid @RequestBody WorkoutCreateRequest request) {
        try {
            Workout workout = workoutService.createWorkout(request);
            return ResponseEntity.ok(Result.ok("运动记录创建成功", workout));
        } catch (Exception e) {
            log.error("创建运动记录失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新运动记录
     */
    @PutMapping("/{workoutId}")
    public ResponseEntity<Result<Workout>> updateWorkout(
            @PathVariable Long workoutId, 
            @Valid @RequestBody WorkoutUpdateRequest request) {
        try {
            Workout workout = workoutService.updateWorkout(workoutId, request);
            return ResponseEntity.ok(Result.ok("运动记录更新成功", workout));
        } catch (Exception e) {
            log.error("更新运动记录失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 获取运动记录详情
     */
    @GetMapping("/{workoutId}")
    public ResponseEntity<Result<Workout>> getWorkout(@PathVariable Long workoutId) {
        try {
            Workout workout = workoutService.getWorkoutById(workoutId);
            if (workout == null) {
                return ResponseEntity.ok(Result.error("运动记录不存在"));
            }
            return ResponseEntity.ok(Result.ok("获取成功", workout));
        } catch (Exception e) {
            log.error("获取运动记录失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 删除运动记录
     */
    @DeleteMapping("/{workoutId}")
    public ResponseEntity<Result<String>> deleteWorkout(@PathVariable Long workoutId) {
        try {
            workoutService.deleteWorkout(workoutId);
            return ResponseEntity.ok(Result.ok("运动记录删除成功"));
        } catch (Exception e) {
            log.error("删除运动记录失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 获取用户运动记录列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Result<List<Workout>>> getUserWorkouts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<Workout> workouts = workoutService.getUserWorkouts(userId, page, size);
            return ResponseEntity.ok(Result.ok("获取成功", workouts));
        } catch (Exception e) {
            log.error("获取用户运动记录失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
    
    /**
     * 按运动类型获取用户记录
     */
    @GetMapping("/user/{userId}/type/{workoutType}")
    public ResponseEntity<Result<List<Workout>>> getUserWorkoutsByType(
            @PathVariable Long userId, 
            @PathVariable String workoutType) {
        try {
            List<Workout> workouts = workoutService.getUserWorkoutsByType(userId, workoutType);
            return ResponseEntity.ok(Result.ok("获取成功", workouts));
        } catch (Exception e) {
            log.error("按类型获取运动记录失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 获取用户指定日期的运动记录
     */
    @GetMapping("/user/{userId}/date/{date}")
    public ResponseEntity<Result<List<Workout>>> getUserWorkoutsByDate(
            @PathVariable Long userId,
            @PathVariable LocalDate date) {
        try {
            List<Workout> workouts = workoutService.getUserWorkoutsByDate(userId, date);
            return ResponseEntity.ok(Result.ok("获取成功", workouts));
        } catch (Exception e) {
            log.error("按日期获取运动记录失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
    
    /**
     * 获取用户今日目标达成状态
     */
    @GetMapping("/user/{userId}/today-goal")
    public ResponseEntity<Result<Map<String, Object>>> getTodayGoalStatus(@PathVariable Long userId) {
        try {
            boolean goalAchieved = workoutService.checkTodayGoalAchievement(userId);
            List<Workout> todayWorkouts = workoutService.getUserWorkoutsByDate(userId, LocalDate.now());
            
            Map<String, Object> response = new HashMap<>();
            response.put("goalAchieved", goalAchieved);
            response.put("todayWorkouts", todayWorkouts.size());
            response.put("workouts", todayWorkouts);
            
            return ResponseEntity.ok(Result.ok("获取成功", response));
        } catch (Exception e) {
            log.error("获取今日目标状态失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 获取用户运动统计
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Result<WorkoutStatsResponse>> getUserWorkoutStats(@PathVariable Long userId) {
        try {
            WorkoutStatsResponse stats = workoutService.getUserWorkoutStats(userId);
            return ResponseEntity.ok(Result.ok("获取统计成功", stats));
        } catch (Exception e) {
            log.error("获取运动统计失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
    
    /**
     * 更新运动状态
     */
    @PutMapping("/{workoutId}/status")
    public ResponseEntity<Result<Workout>> updateWorkoutStatus(
            @PathVariable Long workoutId, 
            @RequestParam String status) {
        try {
            Workout workout = workoutService.updateWorkoutStatus(workoutId, status);
            return ResponseEntity.ok(Result.ok("状态更新成功", workout));
        } catch (Exception e) {
            log.error("更新运动状态失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
    
}
