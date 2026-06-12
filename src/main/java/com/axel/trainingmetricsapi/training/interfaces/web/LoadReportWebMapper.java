package com.axel.trainingmetricsapi.training.interfaces.web;

import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.LoadReportResponse;
import org.springframework.stereotype.Component;

@Component
public class LoadReportWebMapper {

    public LoadReportResponse domainToResponse(LoadReport report) {
        return new LoadReportResponse(
            report.athleteId(),
            report.weekStartDate(),
            report.totalFosterLoad(),
            report.sessionCount(),
            report.updatedAt()
        );
    }
}
