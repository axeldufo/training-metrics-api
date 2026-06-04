package com.axel.trainingmetricsapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoadReportCalculatorTest {

    private static final long ATHLETE_ID = 1L;
    private static final LocalDate WEEK_START = LocalDate.of(2024, Month.JANUARY, 22); // Monday

    private final LoadReportCalculator calculator = new LoadReportCalculator();

    @Test
    void calculate_nominal_3Sessions_returnsSumAndCount() {
        List<TrainingSession> sessions = List.of(
            aSession(5, 60),  // load = 300
            aSession(4, 45),  // load = 180
            aSession(3, 30)   // load = 90
        );

        LoadReport report = calculator.calculate(ATHLETE_ID, WEEK_START, sessions, LocalDateTime.now());

        assertThat(report.athleteId()).isEqualTo(ATHLETE_ID);
        assertThat(report.weekStartDate()).isEqualTo(WEEK_START);
        assertThat(report.totalFosterLoad()).isEqualTo(570);
        assertThat(report.sessionCount()).isEqualTo(3);
        assertThat(report.updatedAt()).isNotNull();
    }

    @Test
    void calculate_singleSession_returnsSingleSessionLoad() {
        TrainingSession session = aSession(8, 45); // load = 360

        LoadReport report = calculator.calculate(ATHLETE_ID, WEEK_START, List.of(session), LocalDateTime.now());

        assertThat(report.totalFosterLoad()).isEqualTo(360);
        assertThat(report.sessionCount()).isEqualTo(1);
    }

    private TrainingSession aSession(int rpe, int durationInMin) {
        return new TrainingSession(WEEK_START, Sport.CYCLING, rpe, durationInMin,
            TargetZone.Z2, ATHLETE_ID);
    }
}
