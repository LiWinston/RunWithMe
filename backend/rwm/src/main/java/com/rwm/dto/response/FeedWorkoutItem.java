package com.rwm.dto.response;

public class FeedWorkoutItem {
    private Long id;
    private Long userId;
    private String userName;
    private String workoutType;
    private Double distanceKm;
    private Integer durationSec;
    private Integer avgPaceSecPerKm;
    private String startTime; // ISO 字符串
    private String summary;   // 预格式化文案，客户端可直接展示

    public FeedWorkoutItem() {}

    public FeedWorkoutItem(Long id, Long userId, String userName, String workoutType, Double distanceKm,
                           Integer durationSec, Integer avgPaceSecPerKm, String startTime, String summary) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.workoutType = workoutType;
        this.distanceKm = distanceKm;
        this.durationSec = durationSec;
        this.avgPaceSecPerKm = avgPaceSecPerKm;
        this.startTime = startTime;
        this.summary = summary;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getWorkoutType() { return workoutType; }
    public void setWorkoutType(String workoutType) { this.workoutType = workoutType; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
    public Integer getAvgPaceSecPerKm() { return avgPaceSecPerKm; }
    public void setAvgPaceSecPerKm(Integer avgPaceSecPerKm) { this.avgPaceSecPerKm = avgPaceSecPerKm; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
