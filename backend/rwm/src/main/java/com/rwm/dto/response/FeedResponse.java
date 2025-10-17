package com.rwm.dto.response;

import java.util.List;

/**
 * FeedResponse 返回用于首页/群组动态的精简 DTO 列表，
 * 避免直接暴露实体，并补充 actorName/targetName、userName、summary 等展示字段。
 */
public class FeedResponse {
    private List<FeedWorkoutItem> workouts;
    private List<FeedInteractionItem> interactions;

    public FeedResponse() {}

    public FeedResponse(List<FeedWorkoutItem> workouts, List<FeedInteractionItem> interactions) {
        this.workouts = workouts;
        this.interactions = interactions;
    }

    public List<FeedWorkoutItem> getWorkouts() { return workouts; }
    public void setWorkouts(List<FeedWorkoutItem> workouts) { this.workouts = workouts; }
    public List<FeedInteractionItem> getInteractions() { return interactions; }
    public void setInteractions(List<FeedInteractionItem> interactions) { this.interactions = interactions; }
}
