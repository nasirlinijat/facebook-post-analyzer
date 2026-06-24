package com.intern.metaanalysis.service;

import com.intern.metaanalysis.model.AnalysisReport;
import com.intern.metaanalysis.model.Post;

import java.util.List;

/**
 * Abstraction over the engagement-analysis logic.
 *
 * <p>Defining this as an interface keeps the web layer independent of the
 * concrete analysis implementation, so the algorithm can evolve or be replaced
 * without touching callers.
 */
public interface DataAnalysisService {

    /**
     * Analyzes the given posts and produces an aggregated report.
     *
     * @param posts the posts to analyze
     * @return the analysis report (top posts, likes by day, best days, summary)
     */
    AnalysisReport analyze(List<Post> posts);
}
