package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.AcwrReport;

public interface GetAcwrReportUseCase {
    AcwrReport execute(long athleteId, long coachId);
}
