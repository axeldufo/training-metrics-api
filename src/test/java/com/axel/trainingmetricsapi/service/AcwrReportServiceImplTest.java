package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.application.port.out.AcwrCachePort;
import com.axel.trainingmetricsapi.domain.AcwrReport;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcwrReportServiceImplTest {

    private static final long ATHLETE_ID = 5L;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private AcwrCachePort acwrCachePort;

    @InjectMocks
    private AcwrReportServiceImpl acwrReportService;

    @Test
    void getAcwrReport_cachePresent() {
        AcwrReport reportInCache = Instancio.create(AcwrReport.class);
        when(acwrCachePort.get(ATHLETE_ID)).thenReturn(Optional.of(reportInCache));

        AcwrReport acwrReport = acwrReportService.getAcwrReport(ATHLETE_ID);

        verifyNoInteractions(trainingSessionRepository);
        verify(acwrCachePort, never()).put(anyLong(), any());
        assertThat(acwrReport).isEqualTo(reportInCache);
    }

    @Test
    void getAcwrReport_cacheNotPresent() {
        when(acwrCachePort.get(ATHLETE_ID)).thenReturn(Optional.empty());
        LocalDate today = LocalDate.now();
        when(trainingSessionRepository.findByAthleteIdAndPeriod(eq(ATHLETE_ID), any(), any()))
            .thenReturn(List.of());

        AcwrReport acwrReport = acwrReportService.getAcwrReport(ATHLETE_ID);

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(trainingSessionRepository).findByAthleteIdAndPeriod(eq(ATHLETE_ID), fromCaptor.capture(), toCaptor.capture());
        assertThat(toCaptor.getValue()).isEqualTo(today);
        assertThat(fromCaptor.getValue()).isEqualTo(today.minusDays(27));
        assertThat(acwrReport.chronicLoad()).isZero();
        verify(acwrCachePort).put(anyLong(), any());
    }

}
