package com.rwm.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupCreateRequest {
    @NotBlank
    private String name;

    @Min(2)
    @Max(50)
    private Integer memberLimit = 6; // default
}
