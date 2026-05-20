package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.CoachAuthData;
import com.axel.trainingmetricsapi.domain.CoachCredentials;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthJpaAdapterTest {

    @Mock
    private CoachJpaRepository coachJpaRepository;

    @InjectMocks
    private AuthJpaAdapter authJpaAdapter;

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        String email = "alice@test.com";
        when(coachJpaRepository.existsByEmail(email)).thenReturn(true);

        boolean result = authJpaAdapter.existsByEmail(email);

        verify(coachJpaRepository).existsByEmail(email);
        assertThat(result).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        String email = "unknown@test.com";
        when(coachJpaRepository.existsByEmail(email)).thenReturn(false);

        boolean result = authJpaAdapter.existsByEmail(email);

        verify(coachJpaRepository).existsByEmail(email);
        assertThat(result).isFalse();
    }

    @Test
    void register_shouldPersistCoachWithHashedPassword() {
        CoachCredentials credentials = new CoachCredentials("Alice Martin", "alice@test.com", "rawPassword");
        String hashedPassword = "hashedPassword123";
        CoachJpaEntity savedEntity = Instancio.create(CoachJpaEntity.class);
        when(coachJpaRepository.save(any())).thenReturn(savedEntity);

        CoachAuthData authDataPersisted = authJpaAdapter.register(credentials, hashedPassword);

        ArgumentCaptor<CoachJpaEntity> captor = ArgumentCaptor.forClass(CoachJpaEntity.class);
        verify(coachJpaRepository).save(captor.capture());
        CoachJpaEntity saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo(credentials.name());
        assertThat(saved.getEmail()).isEqualTo(credentials.email());
        assertThat(saved.getHashedPassword()).isEqualTo(hashedPassword);
        assertThat(authDataPersisted.id()).isEqualTo(savedEntity.getId());
        assertThat(authDataPersisted.hashedPassword()).isEqualTo(savedEntity.getHashedPassword());
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        CoachCredentials credentials = new CoachCredentials("Alice Martin", "alice@test.com", "rawPassword");
        when(coachJpaRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> authJpaAdapter.register(credentials, "hashedPassword"))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByEmail_shouldReturnCoachAuthData_whenEmailExists() {
        CoachJpaEntity entity = Instancio.create(CoachJpaEntity.class);
        when(coachJpaRepository.findByEmail(entity.getEmail())).thenReturn(Optional.of(entity));

        Optional<CoachAuthData> result = authJpaAdapter.findByEmail(entity.getEmail());

        verify(coachJpaRepository).findByEmail(entity.getEmail());
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(entity.getId());
        assertThat(result.get().hashedPassword()).isEqualTo(entity.getHashedPassword());
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        String email = "notfound@test.com";
        when(coachJpaRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<CoachAuthData> result = authJpaAdapter.findByEmail(email);

        verify(coachJpaRepository).findByEmail(email);
        assertThat(result).isEmpty();
    }
}
