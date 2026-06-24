package com.intern.metaanalysis.service.impl;

import com.intern.metaanalysis.config.MetaProperties;
import com.intern.metaanalysis.exception.MetaApiException;
import com.intern.metaanalysis.model.MetaResponse;
import com.intern.metaanalysis.model.Post;
import com.intern.metaanalysis.service.MetaApiClientService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collections;
import java.util.List;

@Service
public class MetaApiClientServiceImpl implements MetaApiClientService {

    private static final String FIELDS =
            "id,message,created_time,likes.summary(true),comments.summary(true)";

    private final RestClient restClient;
    private final MetaProperties metaProperties;

    public MetaApiClientServiceImpl(RestClient restClient, MetaProperties metaProperties) {
        this.restClient = restClient;
        this.metaProperties = metaProperties;
    }

    @Override
    public List<Post> fetchRecentPosts() {
        String url = String.format(
                "https://graph.facebook.com/%s/%s/posts?fields=%s&limit=20&access_token=%s",
                metaProperties.getApiVersion(),
                metaProperties.getPageId(),
                FIELDS,
                metaProperties.getAccessToken());

        try {
            MetaResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(MetaResponse.class);

            if (response == null || response.getData() == null) {
                return Collections.emptyList();
            }
            return response.getData();
        } catch (RestClientResponseException ex) {
            throw new MetaApiException(
                    "Meta Graph API request failed: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString(),
                    ex);
        } catch (Exception ex) {
            throw new MetaApiException("Failed to fetch posts from Meta Graph API", ex);
        }
    }
}
