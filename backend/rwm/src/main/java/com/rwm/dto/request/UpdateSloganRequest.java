package com.rwm.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新健身口号请求DTO
 */
@Data
public class UpdateSloganRequest {
    
    @Size(max = 200, message = "Slogan must not exceed 200 characters")
    private String slogan;
}

