package com.rwm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rwm.dto.request.WorkoutCreateRequest;
import com.rwm.dto.request.WorkoutUpdateRequest;
import com.rwm.dto.response.WorkoutStatsResponse;
import com.rwm.entity.*;
import com.rwm.mapper.*;
import com.rwm.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运动记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutServiceImpl implements WorkoutService {
    
    private final WorkoutMapper workoutMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final NotificationMapper notificationMapper;
    private final UserWeeklyContributionMapper userWeeklyContributionMapper;
    private final UserMapper userMapper;
    
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
            // 如果是完成状态或者距离/时长有变化，尝试检查本周目标达成
            try { checkAndNotifyWeeklyGoalAchieved(workout.getUserId()); } catch (Exception ex) { log.warn("check weekly goal failed: {}", ex.getMessage()); }
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
        try { checkAndNotifyWeeklyGoalAchieved(existingWorkout.getUserId()); } catch (Exception ex) { log.warn("check weekly goal failed: {}", ex.getMessage()); }
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
        // 使用UTC时区保持与数据库一致
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDateTime startOfDay = LocalDate.now(utcZone).atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now(utcZone).atTime(23, 59, 59);
        
        QueryWrapper<Workout> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .between("start_time", startOfDay, endOfDay)
                   .eq("goal_achieved", true);
        
        List<Workout> todayWorkouts = workoutMapper.selectList(queryWrapper);
        return !todayWorkouts.isEmpty();
    }
    
    @Override
    public List<Workout> getUserWorkoutsByDate(Long userId, LocalDate date) {
        // 传入的LocalDate按照UTC时区处理
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        log.info("按日期查询运动记录，用户ID: {}, UTC日期: {}, 时间范围: {} 到 {}", 
                userId, date, startOfDay, endOfDay);
        
        QueryWrapper<Workout> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .between("start_time", startOfDay, endOfDay)
                   .orderByDesc("start_time");
        
        List<Workout> workouts = workoutMapper.selectList(queryWrapper);
        log.info("查询到{}条记录", workouts.size());
        
        return workouts;
    }
    
    @Override
    public WorkoutStatsResponse getUserWorkoutStats(Long userId) {
        log.info("获取用户运动统计，用户ID: {}", userId);
        
        // 查询所有运动记录
        QueryWrapper<Workout> allQuery = new QueryWrapper<>();
        allQuery.eq("user_id", userId).eq("status", "COMPLETED");
        List<Workout> allWorkouts = workoutMapper.selectList(allQuery);
        
        // 查询本周记录 - 使用UTC时区
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDateTime weekStart = LocalDate.now(utcZone).minusDays(7).atStartOfDay();
        QueryWrapper<Workout> weekQuery = new QueryWrapper<>();
        weekQuery.eq("user_id", userId).ge("start_time", weekStart).eq("status", "COMPLETED");
        List<Workout> weekWorkouts = workoutMapper.selectList(weekQuery);
        
        // 查询本月记录 - 使用UTC时区
        LocalDateTime monthStart = LocalDate.now(utcZone).minusDays(30).atStartOfDay();
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
        try { checkAndNotifyWeeklyGoalAchieved(workout.getUserId()); } catch (Exception ex) { log.warn("check weekly goal failed: {}", ex.getMessage()); }
        return workout;
    }
    
    @Override
    public Map<String, Object> getUserTodayStats(Long userId) {
        log.info("获取用户今日统计，用户ID: {}", userId);
        
        // 使用墨尔本时区（用户实际时区）
        ZoneId melbourneZone = ZoneId.of("Australia/Melbourne");
        LocalDateTime startOfDay = LocalDate.now(melbourneZone).atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now(melbourneZone).atTime(23, 59, 59);
        
        // 转换为UTC时间用于数据库查询
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDateTime utcStartOfDay = startOfDay.atZone(melbourneZone).withZoneSameInstant(utcZone).toLocalDateTime();
        LocalDateTime utcEndOfDay = endOfDay.atZone(melbourneZone).withZoneSameInstant(utcZone).toLocalDateTime();
        
        log.info("墨尔本今日时间范围: {} 到 {}", startOfDay, endOfDay);
        log.info("转换为UTC时间范围: {} 到 {}", utcStartOfDay, utcEndOfDay);
        
        QueryWrapper<Workout> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .between("start_time", utcStartOfDay, utcEndOfDay)
                   .eq("status", "COMPLETED");
        
        List<Workout> workouts = workoutMapper.selectList(queryWrapper);
        
        log.info("查询到{}条今日记录", workouts.size());
        
        return aggregateWorkoutStats(workouts);
    }

    // ===== Weekly goal detection & notification =====
    private java.time.LocalDate weekStartUtc(java.time.LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
    }

    private void checkAndNotifyWeeklyGoalAchieved(Long userId) {
        if (userId == null) return;
        // 找到用户当前组
        GroupMember gm = groupMemberMapper.selectOne(new QueryWrapper<GroupMember>().eq("user_id", userId).eq("deleted", false));
        Long gid = gm == null ? null : gm.getGroupId();

        // 只在有组时做通知（feed 按 group_id 汇总）
        if (gid == null) return;

        java.time.LocalDate ws = weekStartUtc(java.time.LocalDate.now());
        java.time.LocalDateTime weekStart = ws.atStartOfDay();

        // 汇总本周已完成的距离（单位：km，database stores in km）
        QueryWrapper<Workout> wq = new QueryWrapper<>();
        wq.eq("user_id", userId).ge("start_time", weekStart).eq("status", "COMPLETED");
        List<Workout> workouts = workoutMapper.selectList(wq);
        java.math.BigDecimal totalKm = java.math.BigDecimal.ZERO;
        for (Workout w : workouts) { 
            if (w.getDistance() != null) totalKm = totalKm.add(w.getDistance()); 
        }

        // 读取用户周目标（单位：km）
        User u = userMapper.selectById(userId);
        Double goalKm = null;
        if (u != null && u.getFitnessGoal() != null && u.getFitnessGoal().getWeeklyDistanceKm() != null) {
            goalKm = u.getFitnessGoal().getWeeklyDistanceKm();
        }
        if (goalKm == null || goalKm <= 0) return;

        // 单位统一：km vs km
        boolean reached = totalKm.doubleValue() >= goalKm;
        if (!reached) return;

        // 查看是否已标记完成
        UserWeeklyContribution c = userWeeklyContributionMapper.selectOne(new QueryWrapper<UserWeeklyContribution>().eq("user_id", userId).eq("week_start", ws));
        if (c != null && Boolean.TRUE.equals(c.getIndividualCompleted())) {
            return; // 已完成过
        }

        // upsert 标记完成
        java.time.LocalDate we = ws.plusDays(6);
        if (c == null) {
            c = new UserWeeklyContribution();
            c.setUserId(userId);
            c.setGroupId(gid);
            c.setWeekStart(ws);
            c.setWeekEnd(we);
            c.setIndividualCompleted(true);
            userWeeklyContributionMapper.insert(c);
        } else {
            c.setIndividualCompleted(true);
            userWeeklyContributionMapper.updateById(c);
        }

        // 发送组内通知（用于 feed 展示）
        Notification n = new Notification();
        n.setUserId(userId); // 收件人可设为自己（feed 以 group_id 汇聚）
        n.setActorUserId(userId);
        n.setTargetUserId(userId);
        n.setGroupId(gid);
        n.setType("WEEKLY_GOAL_ACHIEVED");
        n.setTitle("Weekly goal achieved");
        n.setContent("User reached their weekly goal");
        n.setRead(false);
        n.setCreatedAt(java.time.LocalDateTime.now());
        notificationMapper.insert(n);
    }
    
    @Override
    public Map<String, Object> getUserWeekStats(Long userId) {
        log.info("获取用户本周统计，用户ID: {}", userId);
        
        // 使用UTC时区保持与数据库一致
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDate today = LocalDate.now(utcZone);
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDateTime startOfWeek = weekStart.atStartOfDay();
        LocalDateTime endOfWeek = today.atTime(23, 59, 59);
        
        log.info("UTC本周时间范围: {} 到 {}", startOfWeek, endOfWeek);
        
        QueryWrapper<Workout> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .between("start_time", startOfWeek, endOfWeek)
                   .eq("status", "COMPLETED");
        
        List<Workout> workouts = workoutMapper.selectList(queryWrapper);
        
        return aggregateWorkoutStats(workouts);
    }
    
    @Override
    public Map<String, Object> getUserMonthStats(Long userId) {
        log.info("获取用户本月统计，用户ID: {}", userId);
        
        // 使用UTC时区保持与数据库一致
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDate today = LocalDate.now(utcZone);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime startOfMonth = monthStart.atStartOfDay();
        LocalDateTime endOfMonth = today.atTime(23, 59, 59);
        
        log.info("UTC本月时间范围: {} 到 {}", startOfMonth, endOfMonth);
        
        QueryWrapper<Workout> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .between("start_time", startOfMonth, endOfMonth)
                   .eq("status", "COMPLETED");
        
        List<Workout> workouts = workoutMapper.selectList(queryWrapper);
        
        return aggregateWorkoutStats(workouts);
    }
    
    @Override
    public List<Workout> getUserWorkoutsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("获取用户指定日期范围运动记录，用户ID: {}, 开始: {}, 结束: {}", userId, startDate, endDate);
        
        // 假设传入的日期是墨尔本时区的日期，转换为UTC时区用于数据库查询
        ZoneId melbourneZone = ZoneId.of("Australia/Melbourne");
        ZoneId utcZone = ZoneId.of("UTC");
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // 转换为UTC时间
        LocalDateTime utcStartDateTime = startDateTime.atZone(melbourneZone).withZoneSameInstant(utcZone).toLocalDateTime();
        LocalDateTime utcEndDateTime = endDateTime.atZone(melbourneZone).withZoneSameInstant(utcZone).toLocalDateTime();
        
        log.info("墨尔本日期范围: {} 到 {}", startDateTime, endDateTime);
        log.info("UTC日期范围查询: {} 到 {}", utcStartDateTime, utcEndDateTime);
        
        QueryWrapper<Workout> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .between("start_time", utcStartDateTime, utcEndDateTime)
                   .orderByDesc("start_time");
        
        List<Workout> workouts = workoutMapper.selectList(queryWrapper);
        log.info("查询到{}条指定范围记录", workouts.size());
        
        return workouts;
    }
    
    @Override
    public Map<String, Object> getUserWeeklyChart(Long userId) {
        log.info("获取用户本周图表数据，用户ID: {}", userId);
        
        // 使用UTC时区保持与数据库一致
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDate today = LocalDate.now(utcZone);
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        // 一次性查询整周的数据，避免7次单独查询
        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekEnd.atTime(23, 59, 59);
        
        log.info("本周图表数据查询范围: {} 到 {}", startDateTime, endDateTime);
        
        QueryWrapper<Workout> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .between("start_time", startDateTime, endDateTime)
                   .orderByAsc("start_time");
        
        List<Workout> weekWorkouts = workoutMapper.selectList(queryWrapper);
        log.info("本周图表数据查询到{}条记录", weekWorkouts.size());
        
        Map<String, Object> chartData = new HashMap<>();
        Map<String, Double> dailyDistance = new HashMap<>();
        Map<String, Integer> dailyDuration = new HashMap<>();
        
        // 初始化所有日期为0
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            String dayName = getDayName(date.getDayOfWeek().getValue());
            dailyDistance.put(dayName, 0.0);
            dailyDuration.put(dayName, 0);
        }
        
        // 按日期分组统计
        for (Workout workout : weekWorkouts) {
            LocalDate workoutDate = workout.getStartTime().toLocalDate();
            String dayName = getDayName(workoutDate.getDayOfWeek().getValue());
            
            if (workout.getDistance() != null) {
                dailyDistance.put(dayName, dailyDistance.get(dayName) + workout.getDistance().doubleValue());
            }
            
            if (workout.getDuration() != null) {
                dailyDuration.put(dayName, dailyDuration.get(dayName) + workout.getDuration());
            }
        }
        
        chartData.put("dailyDistance", dailyDistance);
        chartData.put("dailyDuration", dailyDuration);
        chartData.put("labels", new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"});
        
        return chartData;
    }
    
    @Override
    public Map<String, Object> getUserMonthlyChart(Long userId) {
        log.info("获取用户本月图表数据，用户ID: {}", userId);
        
        // 使用UTC时区保持与数据库一致
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDate today = LocalDate.now(utcZone);
        LocalDate monthStart = today.withDayOfMonth(1);
        
        Map<String, Object> chartData = new HashMap<>();
        Map<String, Double> weeklyDistance = new HashMap<>();
        
        // 按周统计本月数据
        LocalDate currentWeekStart = monthStart;
        int weekNumber = 1;
        
        while (currentWeekStart.isBefore(today) || currentWeekStart.isEqual(today)) {
            LocalDate weekEnd = currentWeekStart.plusDays(6);
            if (weekEnd.isAfter(today)) {
                weekEnd = today;
            }
            
            List<Workout> weekWorkouts = getUserWorkoutsByDateRange(userId, currentWeekStart, weekEnd);
            double totalDistance = weekWorkouts.stream()
                    .filter(w -> w.getDistance() != null)
                    .mapToDouble(w -> w.getDistance().doubleValue())
                    .sum();
            
            weeklyDistance.put("Week " + weekNumber, totalDistance);
            
            currentWeekStart = currentWeekStart.plusDays(7);
            weekNumber++;
        }
        
        chartData.put("weeklyDistance", weeklyDistance);
        chartData.put("period", today.getMonth().toString() + " " + today.getYear());
        
        return chartData;
    }
    
    /**
     * 聚合运动统计数据
     */
    private Map<String, Object> aggregateWorkoutStats(List<Workout> workouts) {
        Map<String, Object> stats = new HashMap<>();
        
        double totalDistance = workouts.stream()
                .filter(w -> w.getDistance() != null)
                .mapToDouble(w -> w.getDistance().doubleValue())
                .sum();
        
        int totalDuration = workouts.stream()
                .filter(w -> w.getDuration() != null)
                .mapToInt(Workout::getDuration)
                .sum();
        
        double totalCalories = workouts.stream()
                .filter(w -> w.getCalories() != null)
                .mapToDouble(w -> w.getCalories().doubleValue())
                .sum();
        
        int totalSteps = workouts.stream()
                .filter(w -> w.getSteps() != null)
                .mapToInt(Workout::getSteps)
                .sum();
        
        stats.put("totalDistance", totalDistance);
        stats.put("totalDuration", totalDuration);
        stats.put("totalCalories", totalCalories);
        stats.put("totalSteps", totalSteps);
        stats.put("workoutCount", workouts.size());
        
        return stats;
    }
    
    /**
     * 获取星期名称
     */
    private String getDayName(int dayOfWeek) {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        return days[dayOfWeek - 1];
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
        // 使用UTC时区保持与数据库一致
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDate currentDate = LocalDate.now(utcZone);
        
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
