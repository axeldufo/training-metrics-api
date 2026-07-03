package com.axel.trainingmetricsapi.training.interfaces.web;

import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.LoadReportResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LoadReportWebMapper {
    LoadReportResponse domainToResponse(LoadReport report);
}
