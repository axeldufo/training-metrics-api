package com.axel.trainingmetricsapi.athlete.application;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAthleteUseCaseImplTest {

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private CreateAthleteUseCaseImpl useCase;

    @Test
    void execute_shouldReturnPersistedAthlete_whenAthleteIsSaved() {
        Athlete athlete = Instancio.create(Athlete.class);
        Athlete persisted = Instancio.create(Athlete.class);
        when(athleteRepository.save(athlete)).thenReturn(persisted);

        Athlete result = useCase.execute(athlete);

        verify(athleteRepository).save(athlete);
        assertThat(result).isEqualTo(persisted);
        assertThat(result.getId()).isEqualTo(persisted.getId());
    }
}
