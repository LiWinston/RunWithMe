package com.rwm.dto.response;

public class FeedInteractionItem {
    private Long id;
    private String type;      // LIKE, REMIND, WEEKLY_GOAL_ACHIEVED
    private Long actorUserId;
    private String actorName;
    private Long targetUserId;
    private String targetName;
    private Long groupId;
    private String createdAt; // ISO 字符串
    private String summary;   // 预格式化文案

    public FeedInteractionItem() {}

    public FeedInteractionItem(Long id, String type, Long actorUserId, String actorName, Long targetUserId,
                               String targetName, Long groupId, String createdAt, String summary) {
        this.id = id;
        this.type = type;
        this.actorUserId = actorUserId;
        this.actorName = actorName;
        this.targetUserId = targetUserId;
        this.targetName = targetName;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.summary = summary;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
