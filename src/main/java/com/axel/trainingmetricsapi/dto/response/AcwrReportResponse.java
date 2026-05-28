package com.axel.trainingmetricsapi.dto.response;

import com.axel.trainingmetricsapi.domain.AcwrAlert;

import java.time.LocalDate;

public record AcwrReportResponse(
    long athleteId,
    LocalDate calculatedAt,
    double acuteLoad,
    double chronicLoad,
    double acwr,
    AcwrAlert acwrAlert,
    int weeksOfDataAvailable,
    boolean acwrReliable
) {}
