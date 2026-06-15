package com.axel.trainingmetricsapi.athlete.application;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.identity.domain.CoachRepository;
import com.axel.trainingmetricsapi.identity.domain.exception.CoachNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAthleteUseCaseImplTest {

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private CoachRepository coachRepository;

    @InjectMocks
    private CreateAthleteUseCaseImpl useCase;

    @Test
    void execute_shouldReturnPersistedAthlete_whenAthleteIsSaved() {
        long coachId = 2L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), coachId)
            .create();
        Athlete persisted = Instancio.create(Athlete.class);
        when(coachRepository.existsById(coachId)).thenReturn(true);
        when(athleteRepository.save(athlete)).thenReturn(persisted);

        Athlete result = useCase.execute(athlete);

        verify(coachRepository).existsById(coachId);
        verify(athleteRepository).save(athlete);
        assertThat(result).isEqualTo(persisted);
        assertThat(result.getId()).isEqualTo(persisted.getId());
    }

    @Test
    void execute_shouldThrowCoachNotFoundException_whenCoachDoesNotExist() {
        long coachId = 2L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), coachId)
            .create();
        when(coachRepository.existsById(coachId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(athlete))
            .isInstanceOf(CoachNotFoundException.class);

        verify(coachRepository).existsById(coachId);
        verify(athleteRepository, never()).save(athlete);
    }
}
