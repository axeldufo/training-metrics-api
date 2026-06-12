package com.axel.trainingmetricsapi.training.application.port.in;

import com.axel.trainingmetricsapi.training.domain.AcwrReport;

public interface GetAcwrReportUseCase {
    AcwrReport execute(long athleteId, long coachId);
}
