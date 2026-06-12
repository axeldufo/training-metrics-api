package com.axel.trainingmetricsapi.training.domain;

import com.axel.trainingmetricsapi.shared.domain.exception.DomainValidationException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LoadReport(
    long athleteId,
    LocalDate weekStartDate,
    int totalFosterLoad,
    int sessionCount,
    LocalDateTime updatedAt
) {
    public LoadReport {
        if (weekStartDate == null)
            throw new DomainValidationException("LoadReport weekStartDate is required");
        if (weekStartDate.getDayOfWeek() != DayOfWeek.MONDAY)
            throw new DomainValidationException("LoadReport weekStartDate must be a Monday");
        if (totalFosterLoad < 0)
            throw new DomainValidationException("LoadReport totalFosterLoad must be >= 0");
        if (sessionCount < 0)
            throw new DomainValidationException("LoadReport sessionCount must be >= 0");
        if ((sessionCount == 0) != (totalFosterLoad == 0))
            throw new DomainValidationException("LoadReport sessionCount and totalFosterLoad must both be zero or both be non-zero");
    }
}
