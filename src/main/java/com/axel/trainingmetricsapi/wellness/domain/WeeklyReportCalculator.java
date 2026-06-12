package com.axel.trainingmetricsapi.wellness.domain;

import com.axel.trainingmetricsapi.training.domain.AcwrCalculator;
import com.axel.trainingmetricsapi.training.domain.AcwrReport;
import com.axel.trainingmetricsapi.training.domain.LoadReport;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class WeeklyReportCalculator {

    static final int ABSOLUTE_LOW_THRESHOLD = 2;
    static final int WEEK_OVER_WEEK_THRESHOLD = -2;
    static final double TREND_DECLINING_SLOPE_THRESHOLD = -0.3;
    static final int TREND_DECLINING_WINDOW_WEEKS = 5;
    static final double ACWR_RISING_THRESHOLD = 1.03;
    static final double ACWR_HIGH_THRESHOLD = 1.3;
    static final double ACWR_LOW_THRESHOLD = 0.8;

    private final AcwrCalculator acwrCalculator;

    public WeeklyReportCalculator(AcwrCalculator acwrCalculator) {
        this.acwrCalculator = acwrCalculator;
    }

    public WeeklyReport calculate(
        long athleteId,
        LocalDate weekStartDate,
        List<LoadReport> lastFourLoadReports,
        List<WeeklyWellness> lastFiveWellness
    ) {
        LoadReport currentWeekLoad = lastFourLoadReports.stream()
            .filter(r -> r.weekStartDate().equals(weekStartDate))
            .findFirst()
            .orElse(new LoadReport(athleteId, weekStartDate, 0, 0, null));

        AcwrReport acwrReport = acwrCalculator.computeFromWeeklyLoads(athleteId, weekStartDate, lastFourLoadReports);

        Optional<WeeklyWellness> currentWellnessOpt = lastFiveWellness.stream()
            .filter(w -> w.getWeekStartDate().equals(weekStartDate))
            .findFirst();

        if (currentWellnessOpt.isEmpty()) {
            return new WeeklyReport(
                athleteId, weekStartDate, false,
                currentWeekLoad.totalFosterLoad(), currentWeekLoad.sessionCount(),
                acwrReport.acuteLoad(), acwrReport.chronicLoad(), acwrReport.acwr(),
                acwrReport.acwrAlert(), acwrReport.acwrReliable(),
                null, null, null,
                null, null, null,
                Set.of(), Set.of(), Set.of(),
                CorrelationAlert.INSUFFICIENT_DATA
            );
        }

        WeeklyWellness currentWellness = currentWellnessOpt.get();
        // List is current-week-first, descending — index 0 = current, index 1 = previous
        Optional<WeeklyWellness> previousWellness = lastFiveWellness.stream()
            .filter(w -> w.getWeekStartDate().isBefore(weekStartDate))
            .findFirst();

        Double deltaDifficulty = previousWellness
            .map(p -> (double)(currentWellness.getPerceivedDifficulty() - p.getPerceivedDifficulty()))
            .orElse(null);
        Double deltaFatigue = previousWellness
            .map(p -> (double)(currentWellness.getPerceivedFatigue() - p.getPerceivedFatigue()))
            .orElse(null);
        Double deltaMotivation = previousWellness
            .map(p -> (double)(currentWellness.getMotivation() - p.getMotivation()))
            .orElse(null);

        Set<WellnessAlert> difficultyAlerts = computeWellnessAlerts(
            currentWellness.getPerceivedDifficulty(), deltaDifficulty,
            lastFiveWellness.stream().map(WeeklyWellness::getPerceivedDifficulty).toList());
        Set<WellnessAlert> fatigueAlerts = computeWellnessAlerts(
            currentWellness.getPerceivedFatigue(), deltaFatigue,
            lastFiveWellness.stream().map(WeeklyWellness::getPerceivedFatigue).toList());
        Set<WellnessAlert> motivationAlerts = computeWellnessAlerts(
            currentWellness.getMotivation(), deltaMotivation,
            lastFiveWellness.stream().map(WeeklyWellness::getMotivation).toList());

        CorrelationAlert correlationAlert = computeCorrelationAlert(
            acwrReport.acwr(), deltaFatigue, deltaMotivation);

        return new WeeklyReport(
            athleteId, weekStartDate, true,
            currentWeekLoad.totalFosterLoad(), currentWeekLoad.sessionCount(),
            acwrReport.acuteLoad(), acwrReport.chronicLoad(), acwrReport.acwr(),
            acwrReport.acwrAlert(), acwrReport.acwrReliable(),
            currentWellness.getPerceivedDifficulty(),
            currentWellness.getPerceivedFatigue(),
            currentWellness.getMotivation(),
            deltaDifficulty, deltaFatigue, deltaMotivation,
            difficultyAlerts, fatigueAlerts, motivationAlerts,
            correlationAlert
        );
    }

    private Set<WellnessAlert> computeWellnessAlerts(
        int currentValue,
        Double delta,
        List<Integer> fiveWeeksValues
    ) {
        Set<WellnessAlert> alerts = EnumSet.noneOf(WellnessAlert.class);

        if (currentValue <= ABSOLUTE_LOW_THRESHOLD) {
            alerts.add(WellnessAlert.ABSOLUTE_LOW);
        }
        if (delta != null && delta <= WEEK_OVER_WEEK_THRESHOLD) {
            alerts.add(WellnessAlert.WEEK_OVER_WEEK);
        }
        if (fiveWeeksValues.size() >= TREND_DECLINING_WINDOW_WEEKS) {
            double slope = computeLinearRegressionSlope(fiveWeeksValues);
            if (slope <= TREND_DECLINING_SLOPE_THRESHOLD) {
                alerts.add(WellnessAlert.TREND_DECLINING);
            }
        }

        return Set.copyOf(alerts);
    }

    // Least-squares linear regression slope over the provided values.
    // Finds the best-fit line y = slope*x + intercept (y = a*x + b) -> returns slope only (a).
    private double computeLinearRegressionSlope(List<Integer> weeklyValues) {
        int n = weeklyValues.size();
        double sumX = 0;   // Σ(x)
        double sumY = 0;   // Σ(y)
        double sumXY = 0;  // Σ(x*y)
        double sumX2 = 0;  // Σ(x²)

        for (int i = 0; i < n; i++) {
            double y = weeklyValues.get(n - 1 - i); // 0 = oldest, n-1 = most recent
            sumX += i; // x = week index
            sumY += y; // y = indicator value (fatigue, motivation, etc.)
            sumXY += i * y;
            sumX2 += i * i;
        }

        // return positive slope if trend is rising, negative if declining
        double denominator = n * sumX2 - sumX * sumX;
        return denominator == 0 ? 0.0
            : (n * sumXY - sumX * sumY) / denominator;
    }

    private CorrelationAlert computeCorrelationAlert(double acwr, Double deltaFatigue, Double deltaMotivation) {
        if (deltaFatigue == null) {
            return CorrelationAlert.INSUFFICIENT_DATA;
        }

        boolean fatigueTrendingUp = deltaFatigue > 0;
        boolean motivationDeclining = deltaMotivation < 0;

        if (acwr > ACWR_HIGH_THRESHOLD) {
            return fatigueTrendingUp ? CorrelationAlert.OVERLOAD_RISK : CorrelationAlert.NO_ALERT;
        }
        if (acwr > ACWR_RISING_THRESHOLD) {
            return !fatigueTrendingUp ? CorrelationAlert.GOOD_ADAPTATION : CorrelationAlert.NO_ALERT;
        }
        if (acwr >= ACWR_LOW_THRESHOLD) {
            if (fatigueTrendingUp && motivationDeclining) return CorrelationAlert.POTENTIAL_OVERTRAINING;
            if (fatigueTrendingUp) return CorrelationAlert.STABLE_LOAD_RISING_FATIGUE;
            return CorrelationAlert.NO_ALERT;
        }
        return motivationDeclining ? CorrelationAlert.UNDERLOAD_DECLINING_MOTIVATION : CorrelationAlert.NO_ALERT;
    }
}
