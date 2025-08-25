package com.rwm.dto.request;

import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * 用户注册请求DTO
 */
@Data
public class RegisterRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度应在3-30个字符之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度应在6-100个字符之间")
    private String password;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @NotBlank(message = "姓不能为空")
    private String firstName;
    
    @NotBlank(message = "名不能为空")
    private String lastName;
    
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "性别必须是MALE、FEMALE或OTHER")
    private String gender;
    
    @Min(value = 13, message = "年龄不能小于13岁")
    @Max(value = 120, message = "年龄不能大于120岁")
    private Integer age;
    
    private String phoneNumber;
    
    @Pattern(regexp = "^(BEGINNER|INTERMEDIATE|ADVANCED)$", message = "健身水平必须是BEGINNER、INTERMEDIATE或ADVANCED")
    private String fitnessLevel;
    
    @DecimalMin(value = "50.0", message = "身高不能小于50cm")
    @DecimalMax(value = "300.0", message = "身高不能大于300cm")
    private Double height;
    
    @DecimalMin(value = "20.0", message = "体重不能小于20kg")
    @DecimalMax(value = "500.0", message = "体重不能大于500kg")
    private Double weight;
    
    private String fitnessGoal;
    
    private String weeklyAvailability;
}
