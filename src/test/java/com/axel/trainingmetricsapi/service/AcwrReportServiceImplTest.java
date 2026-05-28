package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.AcwrAlert;
import com.axel.trainingmetricsapi.domain.AcwrReport;
import com.axel.trainingmetricsapi.domain.Sport;
import com.axel.trainingmetricsapi.domain.TargetZone;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcwrReportServiceImplTest {

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @InjectMocks
    private AcwrReportServiceImpl acwrReportService;

    @Test
    void getAcwrReport_shouldCallRepositoryWithCorrectDates() {
        long athleteId = 5L;
        LocalDate today = LocalDate.now();
        when(trainingSessionRepository.findByAthleteIdAndPeriod(eq(athleteId), any(), any()))
            .thenReturn(List.of());

        AcwrReport acwrReport = acwrReportService.getAcwrReport(athleteId);

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(trainingSessionRepository).findByAthleteIdAndPeriod(eq(athleteId), fromCaptor.capture(), toCaptor.capture());
        assertThat(toCaptor.getValue()).isEqualTo(today);
        assertThat(fromCaptor.getValue()).isEqualTo(today.minusDays(27));
        assertThat(acwrReport.chronicLoad()).isZero();
    }

}
