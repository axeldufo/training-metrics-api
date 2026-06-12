package com.axel.trainingmetricsapi.training.domain;

import org.junit.jupiter.api.Test;

import static com.axel.trainingmetricsapi.training.domain.TargetZone.Z3;
import static org.assertj.core.api.Assertions.assertThat;

class TargetZoneTest {

    @Test
    void rpeInZone3_shouldNotReturnTooHighOrTooLow() {
        int sessionRpe = 6;

        assertThat(Z3.isRpeTooLow(sessionRpe)).isFalse();
        assertThat(Z3.isRpeTooHigh(sessionRpe)).isFalse();
    }

    @Test
    void rpeAtTheLimitsOfZone3_shouldNotReturnTooHighOrTooLow() {
        int lowRpeLimit = 5;
        int highRpeLimit = 7;

        assertThat(Z3.isRpeTooLow(lowRpeLimit)).isFalse();
        assertThat(Z3.isRpeTooHigh(highRpeLimit)).isFalse();
    }

    @Test
    void rpeAboveZone3_shouldReturnTooHigh() {
        int sessionRpe = 8;

        assertThat(Z3.isRpeTooLow(sessionRpe)).isFalse();
        assertThat(Z3.isRpeTooHigh(sessionRpe)).isTrue();
    }

    @Test
    void rpeBelowZone3_shouldReturnTooLow() {
        int sessionRpe = 4;

        assertThat(Z3.isRpeTooLow(sessionRpe)).isTrue();
        assertThat(Z3.isRpeTooHigh(sessionRpe)).isFalse();
    }

}
