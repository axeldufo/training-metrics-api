package com.axel.trainingmetricsapi.application.training;

import com.axel.trainingmetricsapi.application.port.out.AcwrCachePort;
import com.axel.trainingmetricsapi.domain.AcwrReport;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAcwrReportUseCaseImplTest {

    private static final long ATHLETE_ID = 5L;
    private static final long COACH_ID = 2L;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private AcwrCachePort acwrCachePort;

    @InjectMocks
    private GetAcwrReportUseCaseImpl useCase;

    @Test
    void execute_cachePresent() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        AcwrReport reportInCache = Instancio.create(AcwrReport.class);
        when(acwrCachePort.get(ATHLETE_ID)).thenReturn(Optional.of(reportInCache));

        AcwrReport result = useCase.execute(ATHLETE_ID, COACH_ID);

        verifyNoInteractions(trainingSessionRepository);
        verify(acwrCachePort, never()).put(anyLong(), any());
        assertThat(result).isEqualTo(reportInCache);
    }

    @Test
    void execute_cacheNotPresent() {
        Athlete athlete = anAthleteOwnedByCoach();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
        when(acwrCachePort.get(ATHLETE_ID)).thenReturn(Optional.empty());
        LocalDate today = LocalDate.now();
        when(trainingSessionRepository.findByAthleteIdAndPeriod(eq(ATHLETE_ID), any(), any()))
            .thenReturn(List.of());

        AcwrReport result = useCase.execute(ATHLETE_ID, COACH_ID);

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(trainingSessionRepository).findByAthleteIdAndPeriod(eq(ATHLETE_ID), fromCaptor.capture(), toCaptor.capture());
        assertThat(toCaptor.getValue()).isEqualTo(today);
        assertThat(fromCaptor.getValue()).isEqualTo(today.minusDays(27));
        assertThat(result.chronicLoad()).isZero();
        verify(acwrCachePort).put(anyLong(), any());
    }

    @Test
    void execute_shouldThrow_whenAthleteNotFound() {
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(acwrCachePort, trainingSessionRepository);
    }

    @Test
    void execute_shouldThrow_whenWrongCoach() {
        Athlete athlete = Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), 99L)
            .create();
        when(athleteRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> useCase.execute(ATHLETE_ID, COACH_ID))
            .isInstanceOf(AthleteNotFoundException.class);

        verifyNoInteractions(acwrCachePort, trainingSessionRepository);
    }

    private Athlete anAthleteOwnedByCoach() {
        return Instancio.of(Athlete.class)
            .set(field(Athlete::getCoachId), COACH_ID)
            .create();
    }
}
