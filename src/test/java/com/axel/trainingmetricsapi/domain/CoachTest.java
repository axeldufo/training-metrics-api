package com.axel.trainingmetricsapi.domain;

import com.axel.trainingmetricsapi.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CoachTest {

    @Nested
    class Invariants {

        @Test
        void constructor_shouldThrow_whenNameIsNull() {
            assertThatThrownBy(() -> new Coach(null))
                .isInstanceOf(DomainValidationException.class);
        }

        @Test
        void constructor_shouldThrow_whenNameIsBlank() {
            assertThatThrownBy(() -> new Coach("   "))
                .isInstanceOf(DomainValidationException.class);
        }

    }
}
