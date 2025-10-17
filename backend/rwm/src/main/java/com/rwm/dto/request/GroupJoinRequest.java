package com.rwm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupJoinRequest {
    @NotNull
    private Long groupId;

    // optional inviter (if scanned admin QR that encodes inviter id)
    private Long inviterUserId;
}
