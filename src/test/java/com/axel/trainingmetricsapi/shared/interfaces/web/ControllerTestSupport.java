package com.axel.trainingmetricsapi.shared.interfaces.web;

import com.axel.trainingmetricsapi.identity.interfaces.web.security.JwtUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@Import(ControllerTestSupport.FixedClockConfig.class)
public abstract class ControllerTestSupport {

    @MockitoBean                 // Not used but Required by @WebMvcTest context
    protected JwtUtils jwtUtils; // JwtAuthenticationFilter depends on JwtUtils

    @TestConfiguration
    static class FixedClockConfig { // Provides a fixed Clock bean for controller tests.
        @Bean
        @Primary
        Clock clock() {
            // Pins "today" to 2024-01-12 so that LocalDate.now(clock).plusWeeks(n)
            // always produces a deterministic future date, regardless of when tests run.
            return Clock.fixed(Instant.parse("2026-01-12T00:00:00Z"), ZoneOffset.UTC);
        }
    }

}
