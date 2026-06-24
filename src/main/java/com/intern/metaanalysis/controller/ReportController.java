package com.intern.metaanalysis.controller;

import com.intern.metaanalysis.dto.response.AnalysisReportResponse;
import com.intern.metaanalysis.exception.MetaApiException;
import com.intern.metaanalysis.mapper.AnalysisReportMapper;
import com.intern.metaanalysis.model.AnalysisReport;
import com.intern.metaanalysis.model.Post;
import com.intern.metaanalysis.service.DataAnalysisService;
import com.intern.metaanalysis.service.MetaApiClientService;
import com.intern.metaanalysis.service.SamplePostProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Report", description = "Meta post engagement analysis")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private static final String SOURCE_LIVE = "live";
    private static final String SOURCE_SAMPLE = "sample";

    private final MetaApiClientService metaApiClientService;
    private final DataAnalysisService dataAnalysisService;
    private final SamplePostProvider samplePostProvider;
    private final AnalysisReportMapper analysisReportMapper;

    public ReportController(
            MetaApiClientService metaApiClientService,
            DataAnalysisService dataAnalysisService,
            SamplePostProvider samplePostProvider,
            AnalysisReportMapper analysisReportMapper) {
        this.metaApiClientService = metaApiClientService;
        this.dataAnalysisService = dataAnalysisService;
        this.samplePostProvider = samplePostProvider;
        this.analysisReportMapper = analysisReportMapper;
    }

    @Operation(
            summary = "Generate engagement report",
            description = "Fetches the most recent posts from the configured Meta page and returns "
                    + "the top-3 posts by engagement, likes aggregated by day of week, the best "
                    + "day(s) for likes, and a text summary. If the live Meta data is unavailable "
                    + "(empty Page or API error) the response falls back to a built-in sample "
                    + "dataset, indicated by the `dataSource` field.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report generated successfully")
    })
    @GetMapping("/report")
    public AnalysisReportResponse getReport() {
        List<Post> posts;
        String dataSource;

        try {
            posts = metaApiClientService.fetchRecentPosts();
        } catch (MetaApiException ex) {
            log.warn("Live Meta data unavailable ({}); falling back to sample data.", ex.getMessage());
            posts = List.of();
        }

        if (posts.isEmpty()) {
            log.info("No live posts returned; serving sample dataset.");
            posts = samplePostProvider.getSamplePosts();
            dataSource = SOURCE_SAMPLE;
        } else {
            dataSource = SOURCE_LIVE;
        }

        AnalysisReport report = dataAnalysisService.analyze(posts);
        return analysisReportMapper.toResponse(report, dataSource);
    }
}
