package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.AcwrReport;

public interface AcwrReportService {
    AcwrReport getAcwrReport(long athleteId);
}
