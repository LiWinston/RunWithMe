package com.rwm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.rwm.dto.FitnessGoal;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("users")
public class User {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("username")
    private String username;
    
    @TableField("password")
    private String password;
    
    @TableField("email")
    private String email;
    
    @TableField("email_verified")
    private Boolean emailVerified = false;
    
    @TableField("first_name")
    private String firstName;
    
    @TableField("last_name")
    private String lastName;
    
    @TableField("gender")
    private String gender;
    
    @TableField("age")
    private Integer age;
    
    @TableField("phone_number")
    private String phoneNumber;
    
    @TableField("fitness_level")
    private String fitnessLevel;
    
    @TableField("height")
    private Double height;

    @TableField("weight")
    private Double weight;
    
    @TableField(value = "fitness_goal", typeHandler = JacksonTypeHandler.class)
    private FitnessGoal fitnessGoal;
    
    @TableField("weekly_availability")
    private String weeklyAvailability;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
