package com.rwm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberInteractRequest {
    @NotNull
    private Long targetUserId;

    @NotNull
    private String action; // LIKE or REMIND
}
