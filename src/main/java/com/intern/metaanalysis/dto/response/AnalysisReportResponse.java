package com.intern.metaanalysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Public API response representation of the engagement analysis report.
 */
@Schema(description = "Aggregated engagement analysis report")
public record AnalysisReportResponse(

        @Schema(description = "Top posts ranked by engagement score")
        List<PostResponse> topPosts,

        @Schema(description = "Total likes per day of week (MONDAY..SUNDAY)",
                example = "{\"MONDAY\":120,\"TUESDAY\":80}")
        Map<String, Integer> likesByDay,

        @Schema(description = "Day(s) with the highest cumulative likes", example = "[\"WEDNESDAY\"]")
        List<String> bestDaysForLikes,

        @Schema(description = "Human-readable summary of the analysis")
        String summary,

        @Schema(description = "Origin of the analyzed data: \"live\" (Meta Graph API) or "
                + "\"sample\" (built-in fallback dataset)", example = "live")
        String dataSource
) {
}
