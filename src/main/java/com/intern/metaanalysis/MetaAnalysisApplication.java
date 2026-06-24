package com.intern.metaanalysis;

import com.intern.metaanalysis.config.DotenvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MetaAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MetaAnalysisApplication.class);
        app.addInitializers(new DotenvConfig());
        app.run(args);
    }
}
