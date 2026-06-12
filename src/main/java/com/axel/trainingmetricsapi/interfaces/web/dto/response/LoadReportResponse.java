package com.axel.trainingmetricsapi.interfaces.web.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LoadReportResponse(
    long athleteId,
    LocalDate weekStartDate,
    int totalFosterLoad,
    int sessionCount,
    LocalDateTime updatedAt
) {}
