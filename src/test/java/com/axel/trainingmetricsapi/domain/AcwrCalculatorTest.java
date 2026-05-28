package com.axel.trainingmetricsapi.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class AcwrCalculatorTest {

    private final AcwrCalculator calculator = new AcwrCalculator();

    // today = Jan 28, 2024
    // sessions between Jan 1 and Jan 28 will be included in chronicLoad
    // sessions between Jan 22 and Jan 28 will be included in acuteLoad
    private static final LocalDate TODAY = LocalDate.of(2024, 1, 28);
    static final long ATHLETE_ID = 1L;

    @Nested
    class BusinessLogic {

        @Test
        void calculate_nominal_4WeeksOfData_returnsCorrectFields() {
            // 3 non-acute sessions (load=40 each) + 1 acute session (load=80)
            // total = 200, chronicLoad = 50, acuteLoad = 80, acwr = 1.6 → HIGH
            List<TrainingSession> sessions = List.of(
                aSession(LocalDate.of(2024, 1, 2), 4, 10),
                aSession(LocalDate.of(2024, 1, 9), 4, 10),
                aSession(LocalDate.of(2024, 1, 16), 4, 10),
                aSession(LocalDate.of(2024, 1, 22), 8, 10)
            );

            AcwrReport report = calculator.calculate(ATHLETE_ID, sessions, TODAY);

            assertThat(report.athleteId()).isEqualTo(ATHLETE_ID);
            assertThat(report.calculatedAt()).isEqualTo(TODAY);
            assertThat(report.acuteLoad()).isEqualTo(80.0);
            assertThat(report.chronicLoad()).isEqualTo(50.0);
            assertThat(report.acwr()).isCloseTo(1.6, within(0.001));
            assertThat(report.acwrAlert()).isEqualTo(AcwrAlert.HIGH);
            assertThat(report.weeksOfDataAvailable()).isEqualTo(4);
            assertThat(report.acwrReliable()).isTrue();
        }

        @Test
        void calculate_boundary_acwrBelow0_8_isLow() {
            // 3 non-acute (load=40 each) + 1 acute (load=20)
            // total = 140, chronicLoad = 35, acuteLoad = 20, acwr = 0.571 → LOW
            List<TrainingSession> sessions = List.of(
                aSession(LocalDate.of(2024, 1, 2), 4, 10),
                aSession(LocalDate.of(2024, 1, 9), 4, 10),
                aSession(LocalDate.of(2024, 1, 16), 4, 10),
                aSession(LocalDate.of(2024, 1, 22), 4, 5)
            );

            AcwrReport report = calculator.calculate(ATHLETE_ID, sessions, TODAY);

            assertThat(report.acwrAlert()).isEqualTo(AcwrAlert.LOW);
            assertThat(report.acwrReliable()).isTrue();
        }

        @Test
        void calculate_edge_chronicLoadZero_returnsNoData() {
            AcwrReport report = calculator.calculate(ATHLETE_ID, List.of(), TODAY);

            assertThat(report.calculatedAt()).isEqualTo(TODAY);
            assertThat(report.chronicLoad()).isEqualTo(0.0);
            assertThat(report.acuteLoad()).isEqualTo(0.0);
            assertThat(report.acwr()).isEqualTo(0.0);
            assertThat(report.acwrAlert()).isEqualTo(AcwrAlert.NO_DATA);
            assertThat(report.acwrReliable()).isFalse();
            assertThat(report.weeksOfDataAvailable()).isZero();
        }

        @Test
        void calculate_edge_2WeeksOfData_notReliable() {
            List<TrainingSession> sessions = List.of(
                aSession(LocalDate.of(2024, 1, 15), 5, 60),
                aSession(LocalDate.of(2024, 1, 22), 5, 60)
            );

            AcwrReport report = calculator.calculate(ATHLETE_ID, sessions, TODAY);

            assertThat(report.weeksOfDataAvailable()).isEqualTo(2);
            assertThat(report.acwr()).isEqualTo(1.0);
            assertThat(report.acwrReliable()).isFalse();
            assertThat(report.acwrAlert()).isEqualTo(AcwrAlert.NO_DATA);
        }

        @Test
        void calculate_boundary_acwr0_8_isOk() {
            // 3 non-acute sessions (load=40 each) + 1 acute (load=30)
            // total = 150, chronicLoad = 37.5, acuteLoad = 30, acwr = 0.8 → OK
            List<TrainingSession> sessions = List.of(
                aSession(LocalDate.of(2024, 1, 2), 4, 10),
                aSession(LocalDate.of(2024, 1, 9), 4, 10),
                aSession(LocalDate.of(2024, 1, 16), 4, 10),
                aSession(LocalDate.of(2024, 1, 22), 5, 6)
            );

            AcwrReport report = calculator.calculate(ATHLETE_ID, sessions, TODAY);

            assertThat(report.acwr()).isCloseTo(0.8, within(0.001));
            assertThat(report.acwrAlert()).isEqualTo(AcwrAlert.OK);
            assertThat(report.acwrReliable()).isTrue();
        }

        @Test
        void calculate_boundary_acwr1_3_isOk() {
            // 3 non-acute (load=27 each) + 1 acute (load=39)
            // total = 120, chronicLoad = 30, acuteLoad = 39, acwr = 1.3 → OK
            List<TrainingSession> sessions = List.of(
                aSession(LocalDate.of(2024, 1, 2), 3, 9),
                aSession(LocalDate.of(2024, 1, 9), 3, 9),
                aSession(LocalDate.of(2024, 1, 16), 3, 9),
                aSession(LocalDate.of(2024, 1, 22), 3, 13)
            );

            AcwrReport report = calculator.calculate(ATHLETE_ID, sessions, TODAY);

            assertThat(report.acwr()).isCloseTo(1.3, within(0.001));
            assertThat(report.acwrAlert()).isEqualTo(AcwrAlert.OK);
            assertThat(report.acwrReliable()).isTrue();
        }
    }

    private TrainingSession aSession(LocalDate date, int rpe, int durationInMin) {
        return new TrainingSession(date, Sport.CYCLING, rpe, durationInMin, TargetZone.Z2, 1L);
    }
}
