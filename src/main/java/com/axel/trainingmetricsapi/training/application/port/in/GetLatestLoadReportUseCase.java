package com.axel.trainingmetricsapi.training.application.port.in;

import com.axel.trainingmetricsapi.training.domain.LoadReport;

public interface GetLatestLoadReportUseCase {
    LoadReport execute(long athleteId, long coachId);
}
