package com.axel.trainingmetricsapi.shared.interfaces.web;

import com.axel.trainingmetricsapi.identity.interfaces.web.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.when;

public abstract class ControllerTestSupport {

    @MockitoBean                 // Not used but Required by @WebMvcTest context
    protected JwtUtils jwtUtils; // JwtAuthenticationFilter depends on JwtUtils

    @MockitoBean
    protected Clock clock;       // Provides a fixed Clock bean for controller tests

    @BeforeEach
    void setUpClock() {
        // Pins "today" to 2026-01-12 for deterministic LocalDate.now(clock) calls
        // in controllers (e.g. default 'to' parameter) and test helpers.
        // Note: @PastOrPresent validation ignores this mock in @WebMvcTest context —
        // use hardcoded future dates (e.g. LocalDate.of(2099, ...)) for those tests.
        when(clock.instant()).thenReturn(Instant.parse("2026-01-12T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

}
