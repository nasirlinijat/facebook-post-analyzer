package com.intern.metaanalysis.model;

import java.util.List;
import java.util.Map;

public class AnalysisReport {

    private List<Post> topPosts;
    private Map<String, Integer> likesByDay;
    private List<String> bestDaysForLikes;
    private String summary;

    public AnalysisReport() {}

    public AnalysisReport(
            List<Post> topPosts,
            Map<String, Integer> likesByDay,
            List<String> bestDaysForLikes,
            String summary) {
        this.topPosts = topPosts;
        this.likesByDay = likesByDay;
        this.bestDaysForLikes = bestDaysForLikes;
        this.summary = summary;
    }

    public List<Post> getTopPosts() {
        return topPosts;
    }

    public void setTopPosts(List<Post> topPosts) {
        this.topPosts = topPosts;
    }

    public Map<String, Integer> getLikesByDay() {
        return likesByDay;
    }

    public void setLikesByDay(Map<String, Integer> likesByDay) {
        this.likesByDay = likesByDay;
    }

    public List<String> getBestDaysForLikes() {
        return bestDaysForLikes;
    }

    public void setBestDaysForLikes(List<String> bestDaysForLikes) {
        this.bestDaysForLikes = bestDaysForLikes;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
