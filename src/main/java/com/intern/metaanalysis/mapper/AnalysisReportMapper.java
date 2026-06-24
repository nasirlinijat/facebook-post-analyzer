package com.intern.metaanalysis.mapper;

import com.intern.metaanalysis.dto.response.AnalysisReportResponse;
import com.intern.metaanalysis.model.AnalysisReport;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper from the domain {@link AnalysisReport} to the public
 * {@link AnalysisReportResponse}.
 *
 * <p>Delegates {@code topPosts} mapping to {@link PostMapper}. The {@code dataSource}
 * label is passed in separately by the caller (it is not part of the domain model).
 */
@Mapper(componentModel = "spring", uses = PostMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AnalysisReportMapper {

    @Mapping(target = "dataSource", source = "dataSource")
    AnalysisReportResponse toResponse(AnalysisReport report, String dataSource);
}
