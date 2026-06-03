package com.axel.trainingmetricsapi.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AcwrCalculator {

    private final LoadReportCalculator loadReportCalculator;

    public AcwrCalculator() {
        this.loadReportCalculator = new LoadReportCalculator();
    }

    public AcwrReport calculate(long athleteId, List<TrainingSession> sessions, LocalDate today) {
        LocalDate chronicFrom = today.minusDays(27);
        List<LoadReport> loadReports = sessions.stream()
            .filter(s -> !s.getDate().isBefore(chronicFrom))
            .collect(Collectors.groupingBy(
                s -> s.getDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            ))
            .entrySet().stream()
            .map(e -> loadReportCalculator.calculate(athleteId, e.getKey(), e.getValue(), LocalDateTime.now()))
            .sorted(Comparator.comparing(LoadReport::weekStartDate).reversed())
            .collect(Collectors.toList());

        return computeFromWeeklyLoads(athleteId, today, loadReports);
    }

    AcwrReport computeFromWeeklyLoads(long athleteId, LocalDate weekStartDate,
                                      List<LoadReport> loadReports) {
        int acuteLoad = loadReports.isEmpty() ? 0 : loadReports.getFirst().totalFosterLoad();

        long weeksOfDataAvailable = loadReports.stream()
            .filter(r -> r.sessionCount() > 0)
            .count();

        double totalLoad = loadReports.stream()
            .mapToInt(LoadReport::totalFosterLoad)
            .sum();

        double chronicLoad = weeksOfDataAvailable == 0 ? 0.0 : totalLoad / weeksOfDataAvailable;
        boolean acwrReliable = weeksOfDataAvailable >= 4;
        double acwr = chronicLoad == 0.0 ? 0.0 : acuteLoad / chronicLoad;
        AcwrAlert acwrAlert = AcwrAlert.from(acwr, acwrReliable);

        return new AcwrReport(athleteId, weekStartDate, acuteLoad, chronicLoad,
            acwr, acwrAlert, (int) weeksOfDataAvailable, acwrReliable);
    }
}
