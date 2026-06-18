package com.axel.trainingmetricsapi.training.domain;

import com.axel.trainingmetricsapi.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoadReportTest {

    private static final long ATHLETE_ID = 1L;
    private static final LocalDate MONDAY = LocalDate.of(2026, Month.JANUARY, 12); // 12/01/26 is a Monday

    @Nested
    class Invariants {

        @Test
        void constructor_shouldThrow_whenWeekStartDateIsNull() {
            assertThatThrownBy(() -> new LoadReport(ATHLETE_ID, null, 100, 1, null))
                .isInstanceOf(DomainValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6})
        void constructor_shouldThrow_whenWeekStartDateIsNotMonday(int daysAfterMonday) {
            LocalDate nonMonday = MONDAY.plusDays(daysAfterMonday);
            assertThatThrownBy(() -> new LoadReport(ATHLETE_ID, nonMonday, 100, 1, null))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenTotalFosterLoadIsNegative() {
            assertThatThrownBy(() -> new LoadReport(ATHLETE_ID, MONDAY, -1, 1, null))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenSessionCountIsNegative() {
            assertThatThrownBy(() -> new LoadReport(ATHLETE_ID, MONDAY, 100, -1, null))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenSessionsExistButLoadIsZero() {
            assertThatThrownBy(() -> new LoadReport(ATHLETE_ID, MONDAY, 0, 3, null))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenLoadIsPositiveButSessionCountIsZero() {
            assertThatThrownBy(() -> new LoadReport(ATHLETE_ID, MONDAY, 100, 0, null))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldAllowZeroLoadWithZeroSessions() {
            assertThatNoException().isThrownBy(() -> new LoadReport(ATHLETE_ID, MONDAY, 0, 0, null));
        }

        @Test
        void constructor_shouldAllowNullUpdatedAt() {
            assertThatNoException().isThrownBy(() -> new LoadReport(ATHLETE_ID, MONDAY, 100, 1, null));
        }

        @Test
        void constructor_shouldAllowNonNullUpdatedAt() {
            assertThatNoException().isThrownBy(() -> new LoadReport(ATHLETE_ID, MONDAY, 100, 1,
                LocalDateTime.of(2026, Month.JANUARY, 12, 10, 0)));
        }

        @Test
        void constructor_shouldStoreAllFields() {
            LocalDateTime updatedAt =  LocalDateTime.of(2026, Month.JANUARY, 12, 10, 0);

            LoadReport report = new LoadReport(ATHLETE_ID, MONDAY, 300, 5, updatedAt);

            assertThat(report.athleteId()).isEqualTo(ATHLETE_ID);
            assertThat(report.weekStartDate()).isEqualTo(MONDAY);
            assertThat(report.totalFosterLoad()).isEqualTo(300);
            assertThat(report.sessionCount()).isEqualTo(5);
            assertThat(report.updatedAt()).isEqualTo(updatedAt);
        }
    }
}
