package com.intern.metaanalysis.service;

import com.intern.metaanalysis.model.Post;

import java.util.List;

/**
 * Supplies a built-in sample dataset used as a fallback when the live Meta Graph
 * API returns no data (e.g. an empty/suspended Page) or fails.
 *
 * <p>This keeps the report endpoint — and the deployed demo — useful even when a
 * live Page token or active Page is temporarily unavailable. Live data always
 * takes precedence; the sample data is clearly labelled in the response.
 */
public interface SamplePostProvider {

    /**
     * @return the built-in sample posts (never {@code null})
     */
    List<Post> getSamplePosts();
}
