package com.rwm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationItem {
    private Long id;
    private Long userId;
    private String userName;
    private Long groupId;
    private String groupName;
    private Long timestamp;
    private String status; // PENDING/APPROVED/REJECTED
}
