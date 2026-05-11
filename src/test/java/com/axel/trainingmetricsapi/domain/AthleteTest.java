package com.axel.trainingmetricsapi.domain;

import com.axel.trainingmetricsapi.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AthleteTest {

    @Nested
    class Invariants {

        @Test
        void constructor_shouldThrow_whenFirstNameIsNull() {
            assertThatThrownBy(() -> new Athlete(null, "Dupont", LocalDate.now(), Sport.CYCLING, 2L, 80.0))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenFirstNameIsBlank() {
            assertThatThrownBy(() -> new Athlete("   ", "Dupont", LocalDate.now(), Sport.CYCLING, 2L, 80.0))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenLastNameIsNull() {
            assertThatThrownBy(() -> new Athlete("Jean", null, LocalDate.now(), Sport.CYCLING, 2L, 80.0))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenLastNameIsBlank() {
            assertThatThrownBy(() -> new Athlete("Jean", "   ", LocalDate.now(), Sport.CYCLING, 2L, 80.0))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenSportIsNull() {
            assertThatThrownBy(() -> new Athlete("Jean", "Dupont", LocalDate.now(), null, 2L, 80.0))
                .isInstanceOf(DomainValidationException.class);
        }

    }

}
