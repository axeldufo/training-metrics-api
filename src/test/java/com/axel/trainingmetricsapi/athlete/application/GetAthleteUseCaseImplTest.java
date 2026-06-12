package com.axel.trainingmetricsapi.athlete.application;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAthleteUseCaseImplTest {

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private GetAthleteUseCaseImpl useCase;

    @Test
    void execute_shouldReturnAthlete_whenAthleteIsFound() {
        long athleteId = 4L;
        long coachId = 2L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), coachId)
            .create();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));

        Athlete result = useCase.execute(athleteId, coachId);

        verify(athleteRepository).findById(athleteId);
        assertThat(result).isEqualTo(athlete);
        assertThat(result.getId()).isEqualTo(athlete.getId());
    }

    @Test
    void execute_shouldThrowException_whenAthleteNotFound() {
        long athleteId = 4L;
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(athleteId, 2L))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(athleteId);
    }

    @Test
    void execute_shouldThrowException_whenAthleteDoesntBelongToRequestingCoach() {
        long athleteId = 4L;
        long requestingCoachId = 2L;
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 8L)
            .create();
        when(athleteRepository.findById(athleteId)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(athleteId, requestingCoachId))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(athleteRepository).findById(athleteId);
    }
}
