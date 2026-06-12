package com.axel.trainingmetricsapi.training.domain;

import java.time.LocalDate;

public record AcwrReport(
    long athleteId,
    LocalDate calculatedAt,
    double acuteLoad,
    double chronicLoad,
    double acwr,
    AcwrAlert acwrAlert,
    int weeksOfDataAvailable,
    boolean acwrReliable) {
}
