package com.axel.trainingmetricsapi.application.athlete;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAthleteUseCaseImplTest {

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private UpdateAthleteUseCaseImpl useCase;

    @Test
    void execute_shouldReturnPersistedAthlete_whenAthleteIsUpdated() {
        long coachId = 2L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), coachId)
            .create();
        Long athleteId = athlete.getId();
        Athlete existing = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), coachId)
            .create();
        Athlete persisted = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), coachId)
            .create();

        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(existing));
        when(athleteRepository.save(athlete)).thenReturn(persisted);

        Athlete result = useCase.execute(athlete, coachId);

        verify(athleteRepository).findById(athleteId);
        verify(athleteRepository).save(athlete);
        assertThat(result).isEqualTo(persisted);
        assertThat(result.getId()).isEqualTo(persisted.getId());
    }

    @Test
    void execute_shouldThrowException_whenAthleteNotFound() {
        long coachId = 2L;
        Athlete athlete = Instancio.create(Athlete.class);
        Long athleteId = athlete.getId();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(athlete, coachId))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(athleteId);
        verify(athleteRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowException_whenAthleteDoesntBelongToRequestingCoach() {
        long requestingCoachId = 2L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), requestingCoachId)
            .create();
        Long athleteId = athlete.getId();
        Athlete existing = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 8L)
            .create();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(athlete, requestingCoachId))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(athleteId);
        verify(athleteRepository, never()).save(any());
    }
}
