package com.intern.metaanalysis.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.metaanalysis.model.MetaResponse;
import com.intern.metaanalysis.model.Post;
import com.intern.metaanalysis.service.SamplePostProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class SamplePostProviderImpl implements SamplePostProvider {

    private static final String SAMPLE_RESOURCE = "sample-posts.json";

    private final List<Post> samplePosts;

    public SamplePostProviderImpl(ObjectMapper objectMapper) {
        // Load once at startup; the file ships with the app on the classpath and
        // is in the same shape as a real Graph API response, so it reuses MetaResponse.
        try (InputStream is = new ClassPathResource(SAMPLE_RESOURCE).getInputStream()) {
            MetaResponse response = objectMapper.readValue(is, MetaResponse.class);
            this.samplePosts = response.getData() != null ? List.copyOf(response.getData()) : List.of();
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to load sample posts from " + SAMPLE_RESOURCE, ex);
        }
    }

    @Override
    public List<Post> getSamplePosts() {
        return samplePosts;
    }
}
