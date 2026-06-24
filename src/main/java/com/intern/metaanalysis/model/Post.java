package com.intern.metaanalysis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {

    private String id;
    private String message;

    @JsonProperty("created_time")
    private String createdTime;

    private Engagement likes;
    private Engagement comments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public Engagement getLikes() {
        return likes;
    }

    public void setLikes(Engagement likes) {
        this.likes = likes;
    }

    public Engagement getComments() {
        return comments;
    }

    public void setComments(Engagement comments) {
        this.comments = comments;
    }

    public int getTotalLikes() {
        return likes != null ? likes.getTotalCount() : 0;
    }

    public int getTotalComments() {
        return comments != null ? comments.getTotalCount() : 0;
    }

    public int getEngagementScore() {
        return getTotalLikes() + getTotalComments();
    }
}
