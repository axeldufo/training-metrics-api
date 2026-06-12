package com.axel.trainingmetricsapi.training.interfaces.web.dto;

import com.axel.trainingmetricsapi.training.domain.AcwrAlert;

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
