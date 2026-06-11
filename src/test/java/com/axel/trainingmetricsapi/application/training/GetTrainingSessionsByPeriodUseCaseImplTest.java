package com.axel.trainingmetricsapi.application.training;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTrainingSessionsByPeriodUseCaseImplTest {

    private static final long ATHLETE_ID = 4L;
    private static final long COACH_ID = 2L;
    private static final LocalDate FROM = LocalDate.of(2024, Month.JANUARY, 1);
    private static final LocalDate TO = LocalDate.of(2024, Month.JANUARY, 31);

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @InjectMocks
    private GetTrainingSessionsByPeriodUseCaseImpl useCase;

    @Test
    void execute_shouldReturnList_whenAthleteExists() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        List<TrainingSession> sessions = Instancio.ofList(TrainingSession.class).size(3).create();
        when(trainingSessionRepository.findByAthleteIdAndPeriod(ATHLETE_ID, FROM, TO)).thenReturn(sessions);

        List<TrainingSession> result = useCase.execute(ATHLETE_ID, COACH_ID, FROM, TO);

        verify(athleteRepository).findById(ATHLETE_ID);
        verify(trainingSessionRepository).findByAthleteIdAndPeriod(ATHLETE_ID, FROM, TO);
        assertThat(result).isEqualTo(sessions);
    }

    @Test
    void execute_shouldThrowException_whenAthleteNotFound() {
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, FROM, TO))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(trainingSessionRepository, never()).findByAthleteIdAndPeriod(anyLong(), any(), any());
    }

    @Test
    void execute_shouldThrowException_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID, FROM, TO))
            .isInstanceOf(AthleteNotFoundException.class);

        verify(trainingSessionRepository, never()).findByAthleteIdAndPeriod(anyLong(), any(), any());
    }
}
