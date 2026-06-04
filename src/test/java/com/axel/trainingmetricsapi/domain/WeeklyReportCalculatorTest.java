package com.axel.trainingmetricsapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class WeeklyReportCalculatorTest {

    private static final long ATHLETE_ID = 1L;
    private static final LocalDate MONDAY = LocalDate.of(2025, 5, 19);
    private static final LocalDate PREV_1 = MONDAY.minusWeeks(1);
    private static final LocalDate PREV_2 = MONDAY.minusWeeks(2);
    private static final LocalDate PREV_3 = MONDAY.minusWeeks(3);
    private static final LocalDate PREV_4 = MONDAY.minusWeeks(4);

    private final WeeklyReportCalculator calculator = new WeeklyReportCalculator(new AcwrCalculator());

    @Test
    void calculate_nominal_4WeeksLoad_5WeeksWellness_returnsAllFieldsCorrect() {
        // acuteLoad=100, total=320, chronicLoad=80, acwr=1.25, GOOD_ADAPTATION
        List<LoadReport> loads = List.of(
            aLoad(MONDAY,  100, 2),
            aLoad(PREV_1,   80, 1),
            aLoad(PREV_2,   60, 1),
            aLoad(PREV_3,   80, 1)
        );
        // current-week-first; deltaFatigue=2-3=-1 (≤0), deltaMotivation=4-4=0
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY,  4, 2, 4),
            aWellness(PREV_1,  3, 3, 4),
            aWellness(PREV_2,  3, 3, 4),
            aWellness(PREV_3,  4, 3, 4),
            aWellness(PREV_4,  4, 3, 5)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.athleteId()).isEqualTo(ATHLETE_ID);
        assertThat(report.weekStartDate()).isEqualTo(MONDAY);
        assertThat(report.wellnessAvailable()).isTrue();
        assertThat(report.totalFosterLoad()).isEqualTo(100);
        assertThat(report.sessionCount()).isEqualTo(2);
        assertThat(report.acuteLoad()).isEqualTo(100.0);
        assertThat(report.chronicLoad()).isEqualTo(80.0);
        assertThat(report.acwr()).isCloseTo(1.25, within(0.001));
        assertThat(report.acwrAlert()).isEqualTo(AcwrAlert.OK);
        assertThat(report.acwrReliable()).isTrue();
        assertThat(report.perceivedDifficulty()).isEqualTo(4);
        assertThat(report.perceivedFatigue()).isEqualTo(2);
        assertThat(report.motivation()).isEqualTo(4);
        assertThat(report.deltaDifficulty()).isEqualTo(1.0);
        assertThat(report.deltaFatigue()).isEqualTo(-1.0);
        assertThat(report.deltaMotivation()).isEqualTo(0.0);
        assertThat(report.fatigueAlerts()).contains(WellnessAlert.ABSOLUTE_LOW);
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.GOOD_ADAPTATION);
    }

    @Test
    void calculate_zeroLoadWeek_wellnessPresent_sessionCount0_wellnessAvailableTrue() {
        List<LoadReport> loads = List.of(
            aLoad(MONDAY, 0, 0),
            aLoad(PREV_1, 80, 1),
            aLoad(PREV_2, 60, 1),
            aLoad(PREV_3, 80, 1)
        );
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 2, 4),
            aWellness(PREV_1, 3, 3, 4)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.wellnessAvailable()).isTrue();
        assertThat(report.sessionCount()).isEqualTo(0);
        assertThat(report.totalFosterLoad()).isEqualTo(0);
        assertThat(report.correlationAlert()).isNotEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
    }

    @Test
    void calculate_wellnessUnavailable_returnsInsufficientData_nullFields_emptyAlerts() {
        List<LoadReport> loads = List.of(aLoad(MONDAY, 100, 2));
        List<WeeklyWellness> wellness = List.of(aWellness(PREV_1, 3, 3, 4));

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.wellnessAvailable()).isFalse();
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
        assertThat(report.perceivedDifficulty()).isNull();
        assertThat(report.perceivedFatigue()).isNull();
        assertThat(report.motivation()).isNull();
        assertThat(report.deltaDifficulty()).isNull();
        assertThat(report.deltaFatigue()).isNull();
        assertThat(report.deltaMotivation()).isNull();
        assertThat(report.difficultyAlerts()).isEmpty();
        assertThat(report.fatigueAlerts()).isEmpty();
        assertThat(report.motivationAlerts()).isEmpty();
    }

    @Test
    void calculate_zeroLoad_noWellness_returnsInsufficientData() {
        List<LoadReport> loads = List.of(aLoad(MONDAY, 0, 0));

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, List.of());

        assertThat(report.wellnessAvailable()).isFalse();
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
        assertThat(report.sessionCount()).isEqualTo(0);
    }

    @Test
    void calculate_absoluteLow_motivation2_triggersAlert() {
        List<LoadReport> loads = List.of(aLoad(MONDAY, 100, 1));
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 3, 2),
            aWellness(PREV_1, 3, 3, 3)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.motivationAlerts()).contains(WellnessAlert.ABSOLUTE_LOW);
    }

    @Test
    void calculate_weekOverWeek_fatigue4to2_triggersAlert() {
        List<LoadReport> loads = List.of(aLoad(MONDAY, 100, 1));
        // delta = 2 - 4 = -2 ≤ WEEK_OVER_WEEK_THRESHOLD
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 2, 4),
            aWellness(PREV_1, 3, 4, 4)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.fatigueAlerts()).contains(WellnessAlert.WEEK_OVER_WEEK);
        assertThat(report.deltaFatigue()).isEqualTo(-2.0);
    }

    @Test
    void calculate_trendDeclining_detected_motivation5544to3_triggersAlert() {
        List<LoadReport> loads = List.of(aLoad(MONDAY, 100, 1));
        // oldest→newest: 5→5→4→4→3. Current-first list:
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 3, 3),
            aWellness(PREV_1, 3, 3, 4),
            aWellness(PREV_2, 3, 3, 4),
            aWellness(PREV_3, 3, 3, 5),
            aWellness(PREV_4, 3, 3, 5)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.motivationAlerts()).contains(WellnessAlert.TREND_DECLINING);
    }

    @Test
    void calculate_trendDeclining_notTriggered_motivation5554_slopeAboveThreshold() {
        List<LoadReport> loads = List.of(aLoad(MONDAY, 100, 1));
        // oldest→newest: 5→5→5→5→4. slope ≈ -0.2 (above threshold -0.3)
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 3, 4),
            aWellness(PREV_1, 3, 3, 5),
            aWellness(PREV_2, 3, 3, 5),
            aWellness(PREV_3, 3, 3, 5),
            aWellness(PREV_4, 3, 3, 5)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.motivationAlerts()).doesNotContain(WellnessAlert.TREND_DECLINING);
    }

    @Test
    void calculate_deltaNull_whenSingleWellnessEntry_noPreviewWeek() {
        List<LoadReport> loads = List.of(aLoad(MONDAY, 100, 1));
        List<WeeklyWellness> wellness = List.of(aWellness(MONDAY, 3, 3, 4));

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.deltaDifficulty()).isNull();
        assertThat(report.deltaFatigue()).isNull();
        assertThat(report.deltaMotivation()).isNull();
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
    }

    @Test
    void calculate_correlationAlert_overloadRisk_acwrHigh_fatigueTrendingUp() {
        // acwr=1.6 (HIGH), fatigue rising: delta=+1
        List<LoadReport> loads = List.of(
            aLoad(MONDAY,  200, 2),
            aLoad(PREV_1,  100, 1),
            aLoad(PREV_2,  100, 1),
            aLoad(PREV_3,  100, 1)
        );
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 4, 4),
            aWellness(PREV_1, 3, 3, 4)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.acwr()).isGreaterThan(1.3);
        assertThat(report.deltaFatigue()).isGreaterThan(0.0);
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.OVERLOAD_RISK);
    }

    @Test
    void calculate_correlationAlert_noAlert_acwrHigh_fatigueNotTrendingUp() {
        // acwr > 1.3 but fatigue stable or improving → NO_ALERT
        List<LoadReport> loads = List.of(
            aLoad(MONDAY, 200, 2),
            aLoad(PREV_1, 100, 1),
            aLoad(PREV_2, 100, 1),
            aLoad(PREV_3, 100, 1)
        );
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 2, 4), // deltaFatigue = 2-3 = -1 ≤ 0
            aWellness(PREV_1, 3, 3, 4)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.acwr()).isGreaterThan(1.3);
        assertThat(report.deltaFatigue()).isLessThanOrEqualTo(0.0);
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.NO_ALERT);
    }

    @Test
    void calculate_correlationAlert_boundary_acwrExactly1_3_fatigueTrendingUp_returnsNoAlert() {
        // acwr=1.3: week0=130, week1=week2=week3=90 → total=400, chronicLoad=100, acwr=1.3
        List<LoadReport> loads = List.of(
            aLoad(MONDAY, 130, 1),
            aLoad(PREV_1,  90, 1),
            aLoad(PREV_2,  90, 1),
            aLoad(PREV_3,  90, 1)
        );
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 4, 4),
            aWellness(PREV_1, 3, 3, 4)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.acwr()).isCloseTo(1.3, within(0.001));
        assertThat(report.deltaFatigue()).isGreaterThan(0.0);
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.NO_ALERT);
    }

    @Test
    void calculate_correlationAlert_goodAdaptation_acwrRising_fatigueStableOrImproving() {
        // acwr=1.25 (> 1.03, < 1.3), deltaFatigue=-1 (≤0)
        List<LoadReport> loads = List.of(
            aLoad(MONDAY,  100, 2),
            aLoad(PREV_1,   80, 1),
            aLoad(PREV_2,   60, 1),
            aLoad(PREV_3,   80, 1)
        );
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 2, 4),
            aWellness(PREV_1, 3, 3, 4)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.acwr()).isGreaterThan(1.03);
        assertThat(report.acwr()).isLessThan(1.3);
        assertThat(report.deltaFatigue()).isLessThanOrEqualTo(0.0);
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.GOOD_ADAPTATION);
    }

    @Test
    void calculate_correlationAlert_goodAdaptation_boundary_acwrExactly1_03_notTriggered() {
        // acwr = 1.03 exact: acuteLoad=103, total=400, chronicLoad=100
        // threshold is strict > 1.03 → NO_ALERT, not GOOD_ADAPTATION
        List<LoadReport> loads = List.of(
            aLoad(MONDAY, 103, 1),
            aLoad(PREV_1,  99, 1),
            aLoad(PREV_2,  99, 1),
            aLoad(PREV_3,  99, 1)
        );
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 2, 4),  // deltaFatigue = 2-3 = -1 ≤ 0 → fatigue stable
            aWellness(PREV_1, 3, 3, 4)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.acwr()).isCloseTo(1.03, within(0.001));
        assertThat(report.deltaFatigue()).isLessThanOrEqualTo(0.0);
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.NO_ALERT);
    }

    @Test
    void calculate_correlationAlert_stableLoadRisingFatigue_stableAcwr_fatigueTrendingUp() {
        // acwr=1.0 (stable, 0.8..1.03), fatigue rising delta=+1, motivation stable
        List<LoadReport> loads = List.of(
            aLoad(MONDAY, 100, 1),
            aLoad(PREV_1, 100, 1),
            aLoad(PREV_2, 100, 1),
            aLoad(PREV_3, 100, 1)
        );
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 4, 4),
            aWellness(PREV_1, 3, 3, 4)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.acwr()).isCloseTo(1.0, within(0.001));
        assertThat(report.deltaFatigue()).isGreaterThan(0.0);
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.STABLE_LOAD_RISING_FATIGUE);
    }

    @Test
    void calculate_correlationAlert_potentialOvertraining_stableAcwr_fatigueTrendingUp_motivationDeclining() {
        // acwr=1.0, fatigue rising delta=+1, motivation declining delta=-1
        List<LoadReport> loads = List.of(
            aLoad(MONDAY, 100, 1),
            aLoad(PREV_1, 100, 1),
            aLoad(PREV_2, 100, 1),
            aLoad(PREV_3, 100, 1)
        );
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 4, 3),
            aWellness(PREV_1, 3, 3, 4)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.acwr()).isCloseTo(1.0, within(0.001));
        assertThat(report.deltaFatigue()).isGreaterThan(0.0);
        assertThat(report.deltaMotivation()).isLessThan(0.0);
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.POTENTIAL_OVERTRAINING);
    }

    @Test
    void calculate_correlationAlert_underloadDecliningMotivation_acwrLow_motivationDeclining() {
        // acwr=0.6 (< 0.8), motivation declining delta=-1
        List<LoadReport> loads = List.of(
            aLoad(MONDAY,  60, 1),
            aLoad(PREV_1, 100, 1),
            aLoad(PREV_2, 100, 1),
            aLoad(PREV_3, 100, 1)
        );
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 3, 3),
            aWellness(PREV_1, 3, 3, 4)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.acwr()).isLessThan(0.8);
        assertThat(report.deltaMotivation()).isLessThan(0.0);
        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.UNDERLOAD_DECLINING_MOTIVATION);
    }

    @Test
    void calculate_correlationAlert_noAlert_stableLoad_noTroubles() {
        // acwr=1.0, no fatigue rising, no motivation declining
        List<LoadReport> loads = List.of(
            aLoad(MONDAY, 100, 1),
            aLoad(PREV_1, 100, 1),
            aLoad(PREV_2, 100, 1),
            aLoad(PREV_3, 100, 1)
        );
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 3, 4),
            aWellness(PREV_1, 3, 3, 4)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.NO_ALERT);
    }

    @Test
    void calculate_correlationAlert_insufficientData_whenWellnessUnavailable() {
        List<LoadReport> loads = List.of(aLoad(MONDAY, 100, 1));

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, List.of());

        assertThat(report.correlationAlert()).isEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
    }

    @Test
    void computeLinearRegressionSlope_allValuesIdentical_returnsZero() {
        List<LoadReport> loads = List.of(aLoad(MONDAY, 100, 1));
        List<WeeklyWellness> wellness = List.of(
            aWellness(MONDAY, 3, 3, 4),
            aWellness(PREV_1, 3, 3, 4),
            aWellness(PREV_2, 3, 3, 4),
            aWellness(PREV_3, 3, 3, 4),
            aWellness(PREV_4, 3, 3, 4)
        );

        WeeklyReport report = calculator.calculate(ATHLETE_ID, MONDAY, loads, wellness);

        assertThat(report.motivationAlerts()).doesNotContain(WellnessAlert.TREND_DECLINING);
    }

    private LoadReport aLoad(LocalDate weekStart, int totalFosterLoad, int sessionCount) {
        return new LoadReport(ATHLETE_ID, weekStart, totalFosterLoad, sessionCount,
            sessionCount > 0 ? LocalDateTime.now() : null);
    }

    private WeeklyWellness aWellness(LocalDate weekStart, int difficulty, int fatigue, int motivation) {
        return new WeeklyWellness(ATHLETE_ID, weekStart, difficulty, fatigue, motivation);
    }
}
