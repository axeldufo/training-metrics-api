package com.axel.trainingmetricsapi.interfaces.web;

import com.axel.trainingmetricsapi.domain.WeeklyReport;
import com.axel.trainingmetricsapi.interfaces.web.dto.response.WeeklyReportResponse;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportWebMapper {

    public WeeklyReportResponse domainToResponse(WeeklyReport report) {
        return new WeeklyReportResponse(
            report.athleteId(),
            report.weekStartDate(),
            report.wellnessAvailable(),
            report.totalFosterLoad(),
            report.sessionCount(),
            report.acuteLoad(),
            report.chronicLoad(),
            report.acwr(),
            report.acwrAlert(),
            report.acwrReliable(),
            report.perceivedDifficulty(),
            report.perceivedFatigue(),
            report.motivation(),
            report.deltaDifficulty(),
            report.deltaFatigue(),
            report.deltaMotivation(),
            report.difficultyAlerts(),
            report.fatigueAlerts(),
            report.motivationAlerts(),
            report.correlationAlert()
        );
    }
}
