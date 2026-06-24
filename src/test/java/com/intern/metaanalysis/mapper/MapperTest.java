package com.intern.metaanalysis.mapper;

import com.intern.metaanalysis.dto.response.AnalysisReportResponse;
import com.intern.metaanalysis.dto.response.PostResponse;
import com.intern.metaanalysis.model.AnalysisReport;
import com.intern.metaanalysis.model.Engagement;
import com.intern.metaanalysis.model.Post;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapperTest {

    private final PostMapper postMapper = new PostMapperImpl();
    private final AnalysisReportMapper reportMapper = new AnalysisReportMapperImpl(postMapper);

    private Post post(String id, String message, String createdTime, int likes, int comments) {
        Post post = new Post();
        post.setId(id);
        post.setMessage(message);
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
    void mapsPostFlatteningEngagementMetrics() {
        Post post = post("p1", "Hello", "2026-06-17T10:00:00+0000", 50, 10);

        PostResponse response = postMapper.toResponse(post);

        assertEquals("p1", response.id());
        assertEquals("Hello", response.message());
        assertEquals("2026-06-17T10:00:00+0000", response.createdTime());
        assertEquals(50, response.totalLikes());
        assertEquals(10, response.totalComments());
        assertEquals(60, response.engagementScore());
    }

    @Test
    void mapsPostWithMissingEngagementToZero() {
        Post post = new Post();
        post.setId("p2");

        PostResponse response = postMapper.toResponse(post);

        assertEquals(0, response.totalLikes());
        assertEquals(0, response.totalComments());
        assertEquals(0, response.engagementScore());
    }

    @Test
    void mapsNullPostToNull() {
        assertNull(postMapper.toResponse(null));
    }

    @Test
    void mapsReportDelegatingTopPostsToPostMapper() {
        AnalysisReport report = new AnalysisReport(
                List.of(post("top", "Best", "2026-06-17T10:00:00+0000", 100, 5)),
                Map.of("WEDNESDAY", 100, "MONDAY", 0),
                List.of("WEDNESDAY"),
                "summary text");

        AnalysisReportResponse response = reportMapper.toResponse(report, "live");

        assertEquals("summary text", response.summary());
        assertEquals(List.of("WEDNESDAY"), response.bestDaysForLikes());
        assertEquals(100, response.likesByDay().get("WEDNESDAY"));
        assertEquals("live", response.dataSource());

        assertEquals(1, response.topPosts().size());
        PostResponse top = response.topPosts().get(0);
        assertEquals("top", top.id());
        assertEquals(105, top.engagementScore());
    }

    @Test
    void mapsEmptyReport() {
        AnalysisReport report = new AnalysisReport(List.of(), Map.of(), List.of(), "No posts.");

        AnalysisReportResponse response = reportMapper.toResponse(report, "sample");

        assertTrue(response.topPosts().isEmpty());
        assertTrue(response.bestDaysForLikes().isEmpty());
        assertEquals("No posts.", response.summary());
        assertEquals("sample", response.dataSource());
    }

    @Test
    void mapsNullReportToNull() {
        assertNull(reportMapper.toResponse(null, null));
    }
}
