package com.axel.trainingmetricsapi.training.interfaces.web;

import com.axel.trainingmetricsapi.training.domain.AcwrReport;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.AcwrReportResponse;
import org.springframework.stereotype.Component;

@Component
public class AcwrReportWebMapper {

    public AcwrReportResponse domainToResponse(AcwrReport report) {
        return new AcwrReportResponse(
            report.athleteId(),
            report.calculatedAt(),
            report.acuteLoad(),
            report.chronicLoad(),
            report.acwr(),
            report.acwrAlert(),
            report.weeksOfDataAvailable(),
            report.acwrReliable()
        );
    }
}
