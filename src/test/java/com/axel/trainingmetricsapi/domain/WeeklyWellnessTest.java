package com.axel.trainingmetricsapi.domain;

import com.axel.trainingmetricsapi.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyWellnessTest {

    private static final LocalDate MONDAY = LocalDate.now().with(DayOfWeek.MONDAY);

    @Nested
    class Invariants {

        @Test
        void constructor_shouldThrow_whenAthleteIdIsNull() {
            assertThatThrownBy(() -> new WeeklyWellness(null, MONDAY, 3, 3, 3))
                .isInstanceOf(DomainValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        void constructor_shouldThrow_whenAthleteIdIsNotPositive(long invalidAthleteId) {
            assertThatThrownBy(() -> new WeeklyWellness(invalidAthleteId, MONDAY, 3, 3, 3))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenWeekStartDateIsNull() {
            assertThatThrownBy(() -> new WeeklyWellness(1L, null, 3, 3, 3))
                .isInstanceOf(DomainValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {2, 3, 4, 5, 6, 7}) // Tuesday through Sunday
        void constructor_shouldThrow_whenWeekStartDateIsNotMonday(int dayOfWeek) {
            LocalDate nonMonday = MONDAY.plusDays(dayOfWeek - 1);
            assertThatThrownBy(() -> new WeeklyWellness(1L, nonMonday, 3, 3, 3))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenPerceivedDifficultyIsNull() {
            assertThatThrownBy(() -> new WeeklyWellness(1L, MONDAY, null, 3, 3))
                .isInstanceOf(DomainValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 6, 100})
        void constructor_shouldThrow_whenPerceivedDifficultyIsOutOfRange(int invalid) {
            assertThatThrownBy(() -> new WeeklyWellness(1L, MONDAY, invalid, 3, 3))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenPerceivedFatigueIsNull() {
            assertThatThrownBy(() -> new WeeklyWellness(1L, MONDAY, 3, null, 3))
                .isInstanceOf(DomainValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 6, 100})
        void constructor_shouldThrow_whenPerceivedFatigueIsOutOfRange(int invalid) {
            assertThatThrownBy(() -> new WeeklyWellness(1L, MONDAY, 3, invalid, 3))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenMotivationIsNull() {
            assertThatThrownBy(() -> new WeeklyWellness(1L, MONDAY, 3, 3, null))
                .isInstanceOf(DomainValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 6, 100})
        void constructor_shouldThrow_whenMotivationIsOutOfRange(int invalid) {
            assertThatThrownBy(() -> new WeeklyWellness(1L, MONDAY, 3, 3, invalid))
                .isInstanceOf(DomainValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5})
        void constructor_shouldNotThrow_whenAllValuesAreValid(int value) {
            assertThatNoException().isThrownBy(() -> new WeeklyWellness(1L, MONDAY, value, value, value));
        }

        @Test
        void constructor_shouldStoreAllFields() {
            long athleteId = 42L;
            int difficulty = 3;
            int fatigue = 4;
            int motivation = 2;

            WeeklyWellness wellness = new WeeklyWellness(athleteId, MONDAY, difficulty, fatigue, motivation);

            assertThat(wellness.getAthleteId()).isEqualTo(athleteId);
            assertThat(wellness.getWeekStartDate()).isEqualTo(MONDAY);
            assertThat(wellness.getPerceivedDifficulty()).isEqualTo(difficulty);
            assertThat(wellness.getPerceivedFatigue()).isEqualTo(fatigue);
            assertThat(wellness.getMotivation()).isEqualTo(motivation);
            assertThat(wellness.getId()).isNull();
        }
    }
}
