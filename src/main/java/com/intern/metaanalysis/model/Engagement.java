package com.intern.metaanalysis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Engagement {

    @JsonProperty("summary")
    private Summary summary;

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public int getTotalCount() {
        return summary != null ? summary.getTotalCount() : 0;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {

        @JsonProperty("total_count")
        private int totalCount;

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
    }
}
