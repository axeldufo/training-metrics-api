package com.axel.trainingmetricsapi.interfaces.web.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedCoachResolverTest {

    private AuthenticatedCoachResolver authenticatedCoachResolver;

    @BeforeEach
    void setUp() {
        authenticatedCoachResolver = new AuthenticatedCoachResolver();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolve_shouldReturnAuthenticatedCoach_whenAuthenticationExists() {
        long coachId = 4L;
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            new AuthenticatedCoach(coachId),
            null,
            Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();

        assertThat(coach.id()).isEqualTo(coachId);
    }

    @Test
    void resolve_shouldThrowIllegalStateException_whenAuthenticationIsNull() {
        assertThatThrownBy(() -> authenticatedCoachResolver.resolve())
            .isInstanceOf(IllegalStateException.class);
    }

}
