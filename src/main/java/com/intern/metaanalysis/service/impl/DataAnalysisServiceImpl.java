package com.intern.metaanalysis.service.impl;

import com.intern.metaanalysis.model.AnalysisReport;
import com.intern.metaanalysis.model.Post;
import com.intern.metaanalysis.service.DataAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataAnalysisServiceImpl implements DataAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(DataAnalysisServiceImpl.class);

    private static final DateTimeFormatter META_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    @Override
    public AnalysisReport analyze(List<Post> posts) {
        List<Post> topPosts = findTopPostsByEngagement(posts, 3);
        Map<DayOfWeek, Integer> likesByDayOfWeek = aggregateLikesByDay(posts);
        List<String> bestDays = findBestDays(likesByDayOfWeek);
        Map<String, Integer> likesByDay = toDayNameMap(likesByDayOfWeek);
        String summary = buildSummary(posts, topPosts, bestDays, likesByDayOfWeek);

        return new AnalysisReport(topPosts, likesByDay, bestDays, summary);
    }

    private List<Post> findTopPostsByEngagement(List<Post> posts, int limit) {
        return posts.stream()
                .sorted(Comparator.comparingInt(Post::getEngagementScore).reversed())
                .limit(limit)
                .toList();
    }

    private Map<DayOfWeek, Integer> aggregateLikesByDay(List<Post> posts) {
        Map<DayOfWeek, Integer> likesByDay = new EnumMap<>(DayOfWeek.class);

        for (Post post : posts) {
            if (post.getCreatedTime() == null) {
                continue;
            }
            try {
                DayOfWeek day = ZonedDateTime.parse(post.getCreatedTime(), META_DATE_FORMAT).getDayOfWeek();
                likesByDay.merge(day, post.getTotalLikes(), Integer::sum);
            } catch (DateTimeParseException ex) {
                log.warn("Skipping post {} due to unparseable created_time '{}'",
                        post.getId(), post.getCreatedTime());
            }
        }

        return likesByDay;
    }

    private List<String> findBestDays(Map<DayOfWeek, Integer> likesByDay) {
        if (likesByDay.isEmpty()) {
            return List.of();
        }

        int maxLikes = likesByDay.values().stream().max(Integer::compareTo).orElse(0);

        List<String> bestDays = new ArrayList<>();
        for (Map.Entry<DayOfWeek, Integer> entry : likesByDay.entrySet()) {
            if (entry.getValue() == maxLikes) {
                bestDays.add(entry.getKey().name());
            }
        }
        return bestDays;
    }

    private Map<String, Integer> toDayNameMap(Map<DayOfWeek, Integer> likesByDay) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            result.put(day.name(), likesByDay.getOrDefault(day, 0));
        }
        return result;
    }

    private String buildSummary(
            List<Post> posts,
            List<Post> topPosts,
            List<String> bestDays,
            Map<DayOfWeek, Integer> likesByDay) {
        if (posts.isEmpty()) {
            return "No posts were available to analyze.";
        }

        int totalLikes = posts.stream().mapToInt(Post::getTotalLikes).sum();
        int totalComments = posts.stream().mapToInt(Post::getTotalComments).sum();
        int avgEngagement = (totalLikes + totalComments) / posts.size();

        String topPostInfo = topPosts.isEmpty()
                ? "No top posts identified."
                : String.format(
                        "Top post scored %d engagement (%d likes, %d comments).",
                        topPosts.get(0).getEngagementScore(),
                        topPosts.get(0).getTotalLikes(),
                        topPosts.get(0).getTotalComments());

        String bestDayInfo = bestDays.isEmpty()
                ? "No day-of-week pattern could be determined."
                : String.format(
                        "Best day(s) for likes: %s with %d total likes across analyzed posts.",
                        String.join(", ", bestDays),
                        likesByDay.values().stream().max(Integer::compareTo).orElse(0));

        return String.format(
                "Analyzed %d posts with %d total likes and %d total comments (avg engagement: %d). %s %s",
                posts.size(),
                totalLikes,
                totalComments,
                avgEngagement,
                topPostInfo,
                bestDayInfo);
    }
}
