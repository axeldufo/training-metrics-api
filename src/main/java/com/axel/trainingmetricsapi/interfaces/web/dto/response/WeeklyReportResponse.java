package com.axel.trainingmetricsapi.interfaces.web.dto.response;

import com.axel.trainingmetricsapi.domain.AcwrAlert;
import com.axel.trainingmetricsapi.domain.CorrelationAlert;
import com.axel.trainingmetricsapi.domain.WellnessAlert;

import java.time.LocalDate;
import java.util.Set;

public record WeeklyReportResponse(
    long athleteId,
    LocalDate weekStartDate,
    boolean wellnessAvailable,
    int totalFosterLoad,
    int sessionCount,
    double acuteLoad,
    double chronicLoad,
    double acwr,
    AcwrAlert acwrAlert,
    boolean acwrReliable,
    Integer perceivedDifficulty,
    Integer perceivedFatigue,
    Integer motivation,
    Double deltaDifficulty,
    Double deltaFatigue,
    Double deltaMotivation,
    Set<WellnessAlert> difficultyAlerts,
    Set<WellnessAlert> fatigueAlerts,
    Set<WellnessAlert> motivationAlerts,
    CorrelationAlert correlationAlert
) {}
