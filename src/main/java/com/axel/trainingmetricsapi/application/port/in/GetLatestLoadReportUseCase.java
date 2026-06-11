package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.LoadReport;

public interface GetLatestLoadReportUseCase {
    LoadReport execute(long athleteId, long coachId);
}
