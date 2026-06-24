package com.intern.metaanalysis.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI metaAnalysisOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Meta Graph API Post Performance Analysis")
                        .description("Fetches recent posts from a Meta account via the Graph API, "
                                + "analyzes engagement, and surfaces top-performing content and the "
                                + "best days for generating likes.")
                        .version("1.0.0")
                        .license(new License().name("Internal").url("https://example.com")));
    }
}
