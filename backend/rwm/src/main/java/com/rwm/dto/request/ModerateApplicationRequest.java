package com.rwm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModerateApplicationRequest {
    @NotNull
    private Long applicationId;

    @NotNull
    private Boolean approve; // true approve, false reject

    private String reason;
}
