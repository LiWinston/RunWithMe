package com.rwm.dto.request;

import lombok.Data;

/**
 * 更新用户资料请求
 */
@Data
public class UpdateProfileRequest {
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private String gender; // MALE, FEMALE, OTHER
    
    private Integer age;
    
    private String phoneNumber;
    
    private String fitnessLevel; // BEGINNER, INTERMEDIATE, ADVANCED
    
    private Double height;
    
    private Double weight;
    
    private String weeklyAvailability;
}

