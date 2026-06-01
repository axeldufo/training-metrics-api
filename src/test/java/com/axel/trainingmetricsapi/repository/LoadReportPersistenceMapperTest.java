package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.LoadReport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LoadReportPersistenceMapperTest {

    private final LoadReportPersistenceMapper mapper = new LoadReportPersistenceMapper();

    private static final LocalDate MONDAY = LocalDate.of(2025, 5, 19);

    @Test
    void domainToEntity_shouldMapAllFields_withNullEntityId() {
        LoadReport report = new LoadReport(42L, MONDAY, 250, 3, LocalDateTime.now());

        LoadReportJpaEntity entity = mapper.domainToEntity(report);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getAthlete().getId()).isEqualTo(42L);
        assertThat(entity.getWeekStartDate()).isEqualTo(MONDAY);
        assertThat(entity.getTotalFosterLoad()).isEqualTo(250);
        assertThat(entity.getSessionCount()).isEqualTo(3);
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void entityToDomain_shouldMapAllFields() {
        LocalDateTime updatedAt = LocalDateTime.now();
        AthleteJpaEntity athlete = new AthleteJpaEntity();
        athlete.setId(42L);
        LoadReportJpaEntity entity = new LoadReportJpaEntity(99L, athlete, MONDAY, 250, 3, updatedAt);

        LoadReport report = mapper.entityToDomain(entity);

        assertThat(report.athleteId()).isEqualTo(42L);
        assertThat(report.weekStartDate()).isEqualTo(MONDAY);
        assertThat(report.totalFosterLoad()).isEqualTo(250);
        assertThat(report.sessionCount()).isEqualTo(3);
        assertThat(report.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void entityToDomain_shouldMapNullUpdatedAt() {
        AthleteJpaEntity athlete = new AthleteJpaEntity();
        athlete.setId(1L);
        LoadReportJpaEntity entity = new LoadReportJpaEntity(1L, athlete, MONDAY, 0, 0, null);

        LoadReport report = mapper.entityToDomain(entity);

        assertThat(report.updatedAt()).isNull();
    }
}
