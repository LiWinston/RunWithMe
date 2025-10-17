package com.rwm.dto.response;

import com.rwm.entity.Notification;
import com.rwm.entity.Workout;
import java.util.List;

public class FeedResponse {
    private List<Workout> workouts;
    private List<Notification> interactions;

    public FeedResponse() {}

    public FeedResponse(List<Workout> workouts, List<Notification> interactions) {
        this.workouts = workouts;
        this.interactions = interactions;
    }

    public List<Workout> getWorkouts() { return workouts; }
    public void setWorkouts(List<Workout> workouts) { this.workouts = workouts; }
    public List<Notification> getInteractions() { return interactions; }
    public void setInteractions(List<Notification> interactions) { this.interactions = interactions; }
}
