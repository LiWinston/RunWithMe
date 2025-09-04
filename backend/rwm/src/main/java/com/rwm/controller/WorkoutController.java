package com.rwm.controller;

import com.rwm.entity.Workout;
import com.rwm.mapper.WorkoutMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workouts")
public class WorkoutController {

    @Autowired
    private WorkoutMapper workoutMapper;

    /** 保存一次运动记录 */
    @PostMapping("/save")
    public String saveWorkout(@RequestBody Workout workout) {
        workoutMapper.insert(workout);
        return "Workout saved successfully";
    }

    /** 查询某个用户的运动记录 */
    @GetMapping("/user/{userId}")
    public List<Workout> getUserWorkouts(@PathVariable Long userId) {
        return workoutMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Workout>()
                        .eq("user_id", userId)
        );
    }
}
