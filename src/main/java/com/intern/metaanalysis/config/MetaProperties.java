package com.intern.metaanalysis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MetaProperties {

    @Value("${META_ACCESS_TOKEN}")
    private String accessToken;

    @Value("${META_PAGE_ID}")
    private String pageId;

    @Value("${META_API_VERSION:v25.0}")
    private String apiVersion;

    public String getAccessToken() {
        return accessToken;
    }

    public String getPageId() {
        return pageId;
    }

    public String getApiVersion() {
        return apiVersion;
    }
}
