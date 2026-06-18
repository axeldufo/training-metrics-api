package com.axel.trainingmetricsapi.training.infrastructure.persistence;

import com.axel.trainingmetricsapi.training.domain.LoadReport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class LoadReportPersistenceMapperTest {

    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, Month.JANUARY, 12, 10, 0);
    private static final LocalDate MONDAY = LocalDate.of(2025, Month.MAY, 19);

    private final LoadReportPersistenceMapper mapper = new LoadReportPersistenceMapper();

    @Test
    void domainToEntity_shouldMapAllFields_withNullEntityId() {
        LoadReport report = new LoadReport(42L, MONDAY, 250, 3, UPDATED_AT);

        LoadReportJpaEntity entity = mapper.domainToEntity(report);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getAthleteId()).isEqualTo(42L);
        assertThat(entity.getWeekStartDate()).isEqualTo(MONDAY);
        assertThat(entity.getTotalFosterLoad()).isEqualTo(250);
        assertThat(entity.getSessionCount()).isEqualTo(3);
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void entityToDomain_shouldMapAllFields() {
        LoadReportJpaEntity entity = new LoadReportJpaEntity(99L, 42L, MONDAY, 250, 3, UPDATED_AT);

        LoadReport report = mapper.entityToDomain(entity);

        assertThat(report.athleteId()).isEqualTo(42L);
        assertThat(report.weekStartDate()).isEqualTo(MONDAY);
        assertThat(report.totalFosterLoad()).isEqualTo(250);
        assertThat(report.sessionCount()).isEqualTo(3);
        assertThat(report.updatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void entityToDomain_shouldMapNullUpdatedAt() {
        LoadReportJpaEntity entity = new LoadReportJpaEntity(1L, 1L, MONDAY, 0, 0, null);

        LoadReport report = mapper.entityToDomain(entity);

        assertThat(report.updatedAt()).isNull();
    }
}
