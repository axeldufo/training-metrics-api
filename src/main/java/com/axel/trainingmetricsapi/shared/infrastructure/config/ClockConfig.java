package com.axel.trainingmetricsapi.shared.infrastructure.config;

import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Clock;

@Configuration
public class ClockConfig {

    /**
     * Single UTC clock for the entire application.
     * Inject this wherever LocalDate.now() / LocalDateTime.now() is needed
     * to guarantee timezone consistency and testability.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Wires the application Clock into Hibernate Validator so that
     * @PastOrPresent / @FutureOrPresent use the same UTC reference
     * as the rest of the application — not the JVM default timezone.
     * In tests, overriding the Clock bean with @Primary automatically
     * propagates to Bean Validation via this configuration.
     */
    @Bean
    static LocalValidatorFactoryBean defaultValidator(Clock clock) {
        return new LocalValidatorFactoryBean() {
            @Override
            public void postProcessConfiguration(
                @Nonnull jakarta.validation.Configuration<?> configuration) {
                configuration.clockProvider(() -> clock);
            }
        };
    }

}
