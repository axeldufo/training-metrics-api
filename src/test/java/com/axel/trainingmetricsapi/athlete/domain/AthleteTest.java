package com.axel.trainingmetricsapi.athlete.domain;

import com.axel.trainingmetricsapi.shared.domain.Sport;
import com.axel.trainingmetricsapi.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AthleteTest {

    @Nested
    class Invariants {

        private final LocalDate birthDate = LocalDate.of(1990, Month.JUNE, 15);

        @Test
        void constructor_shouldThrow_whenFirstNameIsNull() {
            assertThatThrownBy(() -> new Athlete(null, "Dupont", birthDate, Sport.CYCLING, 2L, 80.0))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenFirstNameIsBlank() {
            assertThatThrownBy(() -> new Athlete("   ", "Dupont", birthDate, Sport.CYCLING, 2L, 80.0))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenLastNameIsNull() {
            assertThatThrownBy(() -> new Athlete("Jean", null, birthDate, Sport.CYCLING, 2L, 80.0))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenLastNameIsBlank() {
            assertThatThrownBy(() -> new Athlete("Jean", "   ", birthDate, Sport.CYCLING, 2L, 80.0))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenSportIsNull() {
            assertThatThrownBy(() -> new Athlete("Jean", "Dupont", birthDate, null, 2L, 80.0))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenCoachIsNull() {
            assertThatThrownBy(() -> new Athlete("Jean", "Dupont", birthDate, Sport.CYCLING, null, 80.0))
                .isInstanceOf(DomainValidationException.class);
        }

    }

}
