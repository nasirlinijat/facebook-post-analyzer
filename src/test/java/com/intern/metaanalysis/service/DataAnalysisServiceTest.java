package com.intern.metaanalysis.service;

import com.intern.metaanalysis.model.AnalysisReport;
import com.intern.metaanalysis.model.Engagement;
import com.intern.metaanalysis.model.Post;
import com.intern.metaanalysis.service.impl.DataAnalysisServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataAnalysisServiceTest {

    private final DataAnalysisService service = new DataAnalysisServiceImpl();

    private Post post(String id, String createdTime, int likes, int comments) {
        Post post = new Post();
        post.setId(id);
        post.setMessage("post " + id);
        post.setCreatedTime(createdTime);
        post.setLikes(engagement(likes));
        post.setComments(engagement(comments));
        return post;
    }

    private Engagement engagement(int total) {
        Engagement engagement = new Engagement();
        Engagement.Summary summary = new Engagement.Summary();
        summary.setTotalCount(total);
        engagement.setSummary(summary);
        return engagement;
    }

    @Test
    void returnsTopThreePostsOrderedByEngagement() {
        // 2026-06-15 is a Monday, 2026-06-17 a Wednesday.
        List<Post> posts = List.of(
                post("low", "2026-06-15T10:00:00+0000", 1, 0),
                post("high", "2026-06-17T10:00:00+0000", 50, 10),
                post("mid", "2026-06-15T12:00:00+0000", 20, 5),
                post("tiny", "2026-06-15T13:00:00+0000", 0, 1));

        AnalysisReport report = service.analyze(posts);

        assertEquals(3, report.getTopPosts().size());
        assertEquals("high", report.getTopPosts().get(0).getId());
        assertEquals("mid", report.getTopPosts().get(1).getId());
    }

    @Test
    void identifiesBestDayByCumulativeLikes() {
        List<Post> posts = List.of(
                post("a", "2026-06-15T10:00:00+0000", 10, 0), // Monday
                post("b", "2026-06-15T11:00:00+0000", 15, 0), // Monday -> 25 total
                post("c", "2026-06-17T10:00:00+0000", 5, 0));  // Wednesday

        AnalysisReport report = service.analyze(posts);

        assertEquals(List.of("MONDAY"), report.getBestDaysForLikes());
        assertEquals(25, report.getLikesByDay().get("MONDAY"));
        assertEquals(5, report.getLikesByDay().get("WEDNESDAY"));
    }

    @Test
    void handlesEmptyInputGracefully() {
        AnalysisReport report = service.analyze(List.of());

        assertTrue(report.getTopPosts().isEmpty());
        assertTrue(report.getBestDaysForLikes().isEmpty());
        assertEquals("No posts were available to analyze.", report.getSummary());
    }

    @Test
    void skipsPostsWithUnparseableDates() {
        List<Post> posts = List.of(
                post("good", "2026-06-17T10:00:00+0000", 7, 0),
                post("bad", "not-a-date", 99, 0));

        AnalysisReport report = service.analyze(posts);

        // Bad date is skipped in the day aggregation but still counted in totals.
        assertEquals(7, report.getLikesByDay().get("WEDNESDAY"));
        assertEquals(List.of("WEDNESDAY"), report.getBestDaysForLikes());
    }
}
