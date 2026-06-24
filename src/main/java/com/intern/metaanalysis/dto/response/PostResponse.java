package com.intern.metaanalysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Public API response representation of a post.
 *
 * <p>Flattens the Meta Graph API wire format (nested {@code likes.summary.total_count})
 * into a clean, stable response shape that is independent of the external source.
 */
@Schema(description = "A post with flattened engagement metrics")
public record PostResponse(

        @Schema(description = "Meta post identifier", example = "123_456")
        String id,

        @Schema(description = "Post text content", example = "Check out our new product!")
        String message,

        @Schema(description = "ISO-8601 creation time", example = "2026-06-17T10:00:00+0000")
        String createdTime,

        @Schema(description = "Total likes", example = "210")
        int totalLikes,

        @Schema(description = "Total comments", example = "34")
        int totalComments,

        @Schema(description = "Engagement score (likes + comments)", example = "244")
        int engagementScore
) {
}
