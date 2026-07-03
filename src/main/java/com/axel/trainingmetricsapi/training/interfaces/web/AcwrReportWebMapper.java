package com.axel.trainingmetricsapi.training.interfaces.web;

import com.axel.trainingmetricsapi.training.domain.AcwrReport;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.AcwrReportResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AcwrReportWebMapper {
    AcwrReportResponse domainToResponse(AcwrReport report);
}
