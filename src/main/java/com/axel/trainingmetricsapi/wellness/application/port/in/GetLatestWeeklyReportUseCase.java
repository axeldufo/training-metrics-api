package com.axel.trainingmetricsapi.wellness.application.port.in;

import com.axel.trainingmetricsapi.wellness.domain.WeeklyReport;

public interface GetLatestWeeklyReportUseCase {
    WeeklyReport execute(long athleteId, long coachId);
}
