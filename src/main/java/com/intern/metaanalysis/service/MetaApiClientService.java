package com.intern.metaanalysis.service;

import com.intern.metaanalysis.model.Post;

import java.util.List;

/**
 * Abstraction over the external Meta Graph API.
 *
 * <p>Defining this as an interface decouples callers (e.g. controllers) from the
 * concrete HTTP implementation, which makes the dependency easy to mock in tests
 * or swap for a different data source.
 */
public interface MetaApiClientService {

    /**
     * Fetches the most recent posts (up to 20) for the configured Meta page.
     *
     * @return the recent posts, or an empty list if none are available
     */
    List<Post> fetchRecentPosts();
}
