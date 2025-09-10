package com.rwm.controller;

import com.rwm.dto.response.Result;
import com.rwm.entity.Workout;
import com.rwm.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 运动历史记录控制器
 * 提供日/周/月统计数据和历史记录查询
 */
@Slf4j
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final WorkoutService workoutService;

    /**
     * 获取用户今日运动统计
     */
    @GetMapping("/today/{userId}")
    public Result<Map<String, Object>> getTodayStats(@PathVariable Long userId) {
        log.info("获取用户{}今日运动统计", userId);
        try {
            Map<String, Object> stats = workoutService.getUserTodayStats(userId);
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("获取今日统计失败", e);
            return Result.error("获取今日统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户本周运动统计
     */
    @GetMapping("/week/{userId}")
    public Result<Map<String, Object>> getWeekStats(@PathVariable Long userId) {
        log.info("获取用户{}本周运动统计", userId);
        try {
            Map<String, Object> stats = workoutService.getUserWeekStats(userId);
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("获取本周统计失败", e);
            return Result.error("获取本周统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户本月运动统计
     */
    @GetMapping("/month/{userId}")
    public Result<Map<String, Object>> getMonthStats(@PathVariable Long userId) {
        log.info("获取用户{}本月运动统计", userId);
        try {
            Map<String, Object> stats = workoutService.getUserMonthStats(userId);
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("获取本月统计失败", e);
            return Result.error("获取本月统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户今日运动记录列表
     */
    @GetMapping("/workouts/today/{userId}")
    public Result<List<Workout>> getTodayWorkouts(@PathVariable Long userId) {
        log.info("获取用户{}今日运动记录", userId);
        try {
            // 使用UTC时区保持与数据库一致
            ZoneId utcZone = ZoneId.of("UTC");
            LocalDate today = LocalDate.now(utcZone);
            List<Workout> workouts = workoutService.getUserWorkoutsByDateRange(userId, today, today);
            return Result.ok(workouts);
        } catch (Exception e) {
            log.error("获取今日运动记录失败", e);
            return Result.error("获取今日运动记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户本周运动记录列表
     */
    @GetMapping("/workouts/week/{userId}")
    public Result<List<Workout>> getWeekWorkouts(@PathVariable Long userId) {
        log.info("获取用户{}本周运动记录", userId);
        try {
            // 使用UTC时区保持与数据库一致
            ZoneId utcZone = ZoneId.of("UTC");
            LocalDate today = LocalDate.now(utcZone);
            LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
            List<Workout> workouts = workoutService.getUserWorkoutsByDateRange(userId, weekStart, today);
            return Result.ok(workouts);
        } catch (Exception e) {
            log.error("获取本周运动记录失败", e);
            return Result.error("获取本周运动记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户本月运动记录列表
     */
    @GetMapping("/workouts/month/{userId}")
    public Result<List<Workout>> getMonthWorkouts(@PathVariable Long userId) {
        log.info("获取用户{}本月运动记录", userId);
        try {
            // 使用UTC时区保持与数据库一致
            ZoneId utcZone = ZoneId.of("UTC");
            LocalDate today = LocalDate.now(utcZone);
            LocalDate monthStart = today.withDayOfMonth(1);
            List<Workout> workouts = workoutService.getUserWorkoutsByDateRange(userId, monthStart, today);
            return Result.ok(workouts);
        } catch (Exception e) {
            log.error("获取本月运动记录失败", e);
            return Result.error("获取本月运动记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户本周图表数据（每日运动量）
     */
    @GetMapping("/chart/week/{userId}")
    public Result<Map<String, Object>> getWeekChart(@PathVariable Long userId) {
        log.info("获取用户{}本周图表数据", userId);
        try {
            Map<String, Object> chartData = workoutService.getUserWeeklyChart(userId);
            return Result.ok(chartData);
        } catch (Exception e) {
            log.error("获取本周图表数据失败", e);
            return Result.error("获取本周图表数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户本月图表数据（趋势线）
     */
    @GetMapping("/chart/month/{userId}")
    public Result<Map<String, Object>> getMonthChart(@PathVariable Long userId) {
        log.info("获取用户{}本月图表数据", userId);
        try {
            Map<String, Object> chartData = workoutService.getUserMonthlyChart(userId);
            return Result.ok(chartData);
        } catch (Exception e) {
            log.error("获取本月图表数据失败", e);
            return Result.error("获取本月图表数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户指定日期范围的运动记录
     */
    @GetMapping("/workouts/{userId}")
    public Result<List<Workout>> getWorkoutsByDateRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("获取用户{}从{}到{}的运动记录", userId, startDate, endDate);
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
            List<Workout> workouts = workoutService.getUserWorkoutsByDateRange(userId, start, end);
            return Result.ok(workouts);
        } catch (Exception e) {
            log.error("获取指定日期范围运动记录失败", e);
            return Result.error("获取运动记录失败: " + e.getMessage());
        }
    }
}
