package com.axel.trainingmetricsapi.interfaces.web;

import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.interfaces.web.dto.response.LoadReportResponse;
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
