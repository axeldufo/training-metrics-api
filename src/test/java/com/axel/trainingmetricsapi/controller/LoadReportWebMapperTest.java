package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.dto.response.LoadReportResponse;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LoadReportWebMapperTest {

    private final LoadReportWebMapper mapper = new LoadReportWebMapper();

    @Test
    void domainToResponse_shouldMapAllFields() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDateTime updatedAt = LocalDateTime.now();
        LoadReport report = new LoadReport(42L, monday, 250, 3, updatedAt);

        LoadReportResponse response = mapper.domainToResponse(report);

        assertThat(response.athleteId()).isEqualTo(42L);
        assertThat(response.weekStartDate()).isEqualTo(monday);
        assertThat(response.totalFosterLoad()).isEqualTo(250);
        assertThat(response.sessionCount()).isEqualTo(3);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void domainToResponse_shouldMapNullUpdatedAt_forOnTheFlyReport() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        LoadReport report = new LoadReport(42L, monday, 0, 0, null);

        LoadReportResponse response = mapper.domainToResponse(report);

        assertThat(response.updatedAt()).isNull();
        assertThat(response.totalFosterLoad()).isZero();
        assertThat(response.sessionCount()).isZero();
    }
}
