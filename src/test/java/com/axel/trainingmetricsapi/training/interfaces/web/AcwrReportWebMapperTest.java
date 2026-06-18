package com.axel.trainingmetricsapi.training.interfaces.web;

import com.axel.trainingmetricsapi.training.domain.AcwrAlert;
import com.axel.trainingmetricsapi.training.domain.AcwrReport;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.AcwrReportResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class AcwrReportWebMapperTest {

    private final AcwrReportWebMapper mapper = new AcwrReportWebMapper();

    @Test
    void domainToResponse_shouldMapAllFields() {
        AcwrReport report = new AcwrReport(
            5L,
            LocalDate.of(2024, Month.JANUARY, 28),
            120.0,
            100.0,
            1.2,
            AcwrAlert.OK,
            4,
            true
        );

        AcwrReportResponse response = mapper.domainToResponse(report);

        assertThat(response.athleteId()).isEqualTo(5L);
        assertThat(response.calculatedAt()).isEqualTo(LocalDate.of(2024, Month.JANUARY, 28));
        assertThat(response.acuteLoad()).isEqualTo(120.0);
        assertThat(response.chronicLoad()).isEqualTo(100.0);
        assertThat(response.acwr()).isEqualTo(1.2);
        assertThat(response.acwrAlert()).isEqualTo(AcwrAlert.OK);
        assertThat(response.weeksOfDataAvailable()).isEqualTo(4);
        assertThat(response.acwrReliable()).isTrue();
    }

    @Test
    void domainToResponse_shouldMapAcwrReliableFalse() {
        AcwrReport report = new AcwrReport(1L, LocalDate.of(2026, Month.JANUARY, 15), 0.0, 0.0, 0.0,
            AcwrAlert.NO_DATA, 0, false);
        assertThat(mapper.domainToResponse(report).acwrReliable()).isFalse();
    }

}
