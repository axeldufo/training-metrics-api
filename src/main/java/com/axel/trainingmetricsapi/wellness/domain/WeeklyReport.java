package com.axel.trainingmetricsapi.wellness.domain;

import com.axel.trainingmetricsapi.training.domain.AcwrAlert;

import java.time.LocalDate;
import java.util.Set;

public record WeeklyReport(
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
