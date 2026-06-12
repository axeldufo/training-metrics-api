package com.axel.trainingmetricsapi.training.domain;

import com.axel.trainingmetricsapi.shared.domain.exception.DomainValidationException;
import com.axel.trainingmetricsapi.shared.domain.Sport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class TrainingSessionTest {

    @Nested
    class BusinessLogic {

        @Test
        void getFosterLoad_shouldCalculateLoadCorrectly() {
            TrainingSession sv1Session = new TrainingSession(LocalDate.now(), Sport.ROAD_RUNNING, 4, 90,
                TargetZone.Z2, 4L);

            TrainingSession enduranceSession = new TrainingSession(LocalDate.now(), Sport.CYCLING, 2, 120,
                TargetZone.Z1, 2L);

            TrainingSession hitSession = new TrainingSession(LocalDate.now(), Sport.TRAIL_RUNNING, 8, 75,
                TargetZone.Z1, 8L);

            assertThat(sv1Session.getFosterLoad()).isEqualTo(360);
            assertThat(enduranceSession.getFosterLoad()).isEqualTo(240);
            assertThat(hitSession.getFosterLoad()).isEqualTo(600);
        }

        @Test
        void isAboveTargetZone_shouldReturnTrue_whenRpeIsAboveZone() {
            TrainingSession tempoSessionWithRpe8 = new TrainingSession(LocalDate.now(), Sport.CYCLING, 8, 120,
                TargetZone.Z3, 4L);

            assertThat(tempoSessionWithRpe8.isAboveTargetZone()).isTrue();
        }

        @Test
        void isAboveTargetZone_shouldReturnFalse_whenRpeIsInZone() {
            TrainingSession tempoSessionWithRpe6 = new TrainingSession(LocalDate.now(), Sport.CYCLING, 6, 120,
                TargetZone.Z3, 4L);

            assertThat(tempoSessionWithRpe6.isAboveTargetZone()).isFalse();
        }

        @Test
        void isBelowTargetZone_shouldReturnTrue_whenRpeIsBelowZone() {
            TrainingSession hitSessionWithRpe6 = new TrainingSession(LocalDate.now(), Sport.CYCLING, 6, 120,
                TargetZone.Z5, 4L);

            assertThat(hitSessionWithRpe6.isBelowTargetZone()).isTrue();
        }

        @Test
        void isBelowTargetZone_shouldReturnFalse_whenRpeIsInZone() {
            TrainingSession hitSessionWithRpe8 = new TrainingSession(LocalDate.now(), Sport.CYCLING, 8, 120,
                TargetZone.Z5, 4L);

            assertThat(hitSessionWithRpe8.isBelowTargetZone()).isFalse();
        }

    }

    @Nested
    class Invariants {

        private final LocalDate date = LocalDate.now();

        @Test
        void constructor_shouldThrow_whenDateIsNull() {
            assertThatThrownBy(() -> new TrainingSession(null, Sport.ROAD_RUNNING, 4, 90, TargetZone.Z2, 4L))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenSportIsNull() {
            assertThatThrownBy(() -> new TrainingSession(date, null, 4, 90, TargetZone.Z2, 4L))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenRpeIsNull() {
            assertThatThrownBy(() -> new TrainingSession(date, Sport.ROAD_RUNNING, null, 90,
                TargetZone.Z2, 4L))
                .isInstanceOf(DomainValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 11, 100})
        void constructor_shouldThrow_whenRpeIsInvalid(int invalidRpe) {
            assertThatThrownBy(() -> new TrainingSession(date, Sport.ROAD_RUNNING, invalidRpe, 90,
                TargetZone.Z2, 4L))
                .isInstanceOf(DomainValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10})
        void constructor_shouldNotThrow_whenRpeIsValid(int validRpe) {
            assertThatNoException().isThrownBy(() -> new TrainingSession(date, Sport.ROAD_RUNNING,
                validRpe, 90, TargetZone.Z2, 4L));
        }

        @Test
        void constructor_shouldThrow_whenDurationIsNull() {
            assertThatThrownBy(() -> new TrainingSession(date, Sport.ROAD_RUNNING, 4, null,
                TargetZone.Z2, 4L)).isInstanceOf(DomainValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void constructor_shouldThrow_whenDurationIsInvalid(int invalidDuration) {
            assertThatThrownBy(() -> new TrainingSession(date, Sport.ROAD_RUNNING, 4,
                invalidDuration, TargetZone.Z2, 4L)).isInstanceOf(DomainValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 60, 180})
        void constructor_shouldNotThrow_whenDurationIsValid(int invalidDuration) {
            assertThatNoException().isThrownBy(() -> new TrainingSession(date, Sport.ROAD_RUNNING, 4,
                invalidDuration, TargetZone.Z2, 4L));
        }

        @Test
        void constructor_shouldThrow_whenTargetZoneIsNull() {
            assertThatThrownBy(() -> new TrainingSession(date, Sport.ROAD_RUNNING, 4, 90, null, 4L))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenAthleteIdIsNull() {
            assertThatThrownBy(() -> new TrainingSession(date, Sport.ROAD_RUNNING, 4, 90,
                TargetZone.Z2, null))
                .isInstanceOf(DomainValidationException.class);
        }

    }
}
