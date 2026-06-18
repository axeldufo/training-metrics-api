package com.axel.trainingmetricsapi.training.interfaces.web;

import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.LoadReportResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class LoadReportWebMapperTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, Month.JANUARY, 12); // 12/01/26 is a Monday

    private final LoadReportWebMapper mapper = new LoadReportWebMapper();

    @Test
    void domainToResponse_shouldMapAllFields() {
        LocalDateTime updatedAt = LocalDateTime.of(2026, Month.JANUARY, 12, 10, 0);
        LoadReport report = new LoadReport(42L, MONDAY, 250, 3, updatedAt);

        LoadReportResponse response = mapper.domainToResponse(report);

        assertThat(response.athleteId()).isEqualTo(42L);
        assertThat(response.weekStartDate()).isEqualTo(MONDAY);
        assertThat(response.totalFosterLoad()).isEqualTo(250);
        assertThat(response.sessionCount()).isEqualTo(3);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void domainToResponse_shouldMapNullUpdatedAt_forOnTheFlyReport() {
        LoadReport report = new LoadReport(42L, MONDAY, 0, 0, null);

        LoadReportResponse response = mapper.domainToResponse(report);

        assertThat(response.updatedAt()).isNull();
        assertThat(response.totalFosterLoad()).isZero();
        assertThat(response.sessionCount()).isZero();
    }
}
