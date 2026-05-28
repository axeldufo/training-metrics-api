package com.axel.trainingmetricsapi.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class AcwrCalculator {

    public AcwrReport calculate(long athleteId, List<TrainingSession> sessions, LocalDate today) {

        LocalDate acuteFrom = today.minusDays(6);

        double acuteLoad = sessions.stream()
            .filter(s -> !s.getDate().isBefore(acuteFrom))
            .mapToInt(TrainingSession::getFosterLoad)
            .sum();

        long weeksOfDataAvailable = sessions.stream()
            .map(s -> s.getDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
            .distinct()
            .count();

        double totalFosterLoad = sessions.stream()
            .mapToInt(TrainingSession::getFosterLoad)
            .sum();

        double chronicLoad = weeksOfDataAvailable == 0 ? 0.0
            : totalFosterLoad / weeksOfDataAvailable;

        boolean acwrReliable = weeksOfDataAvailable >= 4;
        double acwr = chronicLoad == 0.0 ? 0.0 : acuteLoad / chronicLoad;
        AcwrAlert acwrAlert = AcwrAlert.from(acwr, acwrReliable);

        return new AcwrReport(athleteId, today, acuteLoad, chronicLoad, acwr, acwrAlert,
            (int) weeksOfDataAvailable, acwrReliable);
    }
}
