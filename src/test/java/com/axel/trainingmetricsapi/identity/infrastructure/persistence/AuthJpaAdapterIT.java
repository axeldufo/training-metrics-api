package com.axel.trainingmetricsapi.identity.infrastructure.persistence;

import com.axel.trainingmetricsapi.TestContainersConfiguration;
import com.axel.trainingmetricsapi.identity.domain.AuthRepository;
import com.axel.trainingmetricsapi.identity.domain.CoachAuthData;
import com.axel.trainingmetricsapi.identity.domain.CoachCredentials;
import com.axel.trainingmetricsapi.identity.domain.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestContainersConfiguration.class)
@Transactional
class AuthJpaAdapterIT {

    @Autowired
    private AuthRepository authRepository;

    @Test
    void register_shouldPersistCoachAndBeRetrievableByEmail() {
        CoachCredentials credentials = aCoachCredentials();
        String hashedPassword = "hashedPassword123";

        CoachAuthData saved = authRepository.register(credentials, hashedPassword);

        assertThat(saved.id()).isPositive();
        assertThat(saved.hashedPassword()).isEqualTo(hashedPassword);
    }

    // UNIQUE constraints are only checked at commit time in PostgreSQL.
    // NOT_SUPPORTED suspends the class-level transaction so operations commit immediately.
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void register_shouldThrowEmailAlreadyExistsException_whenEmailAlreadyExists() {
        CoachCredentials aliceFirstCredentials = aCoachCredentials();
        authRepository.register(aliceFirstCredentials, "hashedPassword123");
        CoachCredentials aliceNewCredentials = new CoachCredentials("Alice Dupont", "alice@test.com", "newPassword");

        assertThatThrownBy(() -> authRepository.register(aliceNewCredentials, "hashedPassword789"))
            .isInstanceOf(EmailAlreadyExistsException.class);
    }

    private CoachCredentials aCoachCredentials() {
        return new CoachCredentials("Alice Martin", "alice@test.com", "rawPassword");
    }
}
