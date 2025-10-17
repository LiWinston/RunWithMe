package com.rwm.dto.request;

import lombok.Data;

import jakarta.validation.constraints.*;
import com.rwm.dto.FitnessGoal;

/**
 * 用户注册请求DTO
 */
@Data
public class RegisterRequest {
    
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 30, message = "Username length should be between 3-30 characters")
    private String username;
    
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 100, message = "Password length should be between 6-100 characters")
    private String password;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "First name cannot be empty")
    private String firstName;
    
    @NotBlank(message = "Last name cannot be empty")
    private String lastName;
    
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE or OTHER")
    private String gender;
    
    @Min(value = 13, message = "Age cannot be less than 13")
    @Max(value = 120, message = "Age cannot be greater than 120")
    private Integer age;
    
    private String phoneNumber;
    
    @Pattern(regexp = "^(BEGINNER|INTERMEDIATE|ADVANCED)$", message = "Fitness level must be BEGINNER, INTERMEDIATE or ADVANCED")
    private String fitnessLevel;
    
    @DecimalMin(value = "50.0", message = "Height cannot be less than 50cm")
    @DecimalMax(value = "300.0", message = "Height cannot be greater than 300cm")
    private Double height;
    
    @DecimalMin(value = "20.0", message = "Weight cannot be less than 20kg")
    @DecimalMax(value = "500.0", message = "Weight cannot be greater than 500kg")
    private Double weight;
    
    private FitnessGoal fitnessGoal;
    
    private String weeklyAvailability;
}
