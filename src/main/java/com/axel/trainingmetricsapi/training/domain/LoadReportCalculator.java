package com.axel.trainingmetricsapi.training.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LoadReportCalculator {

    public LoadReport calculate(long athleteId, LocalDate weekStartDate,
                                List<TrainingSession> sessions,
                                LocalDateTime updatedAt) {
        int totalFosterLoad = sessions.stream()
            .mapToInt(TrainingSession::getFosterLoad)
            .sum();
        return new LoadReport(athleteId, weekStartDate, totalFosterLoad, sessions.size(), updatedAt);
    }
}
