package com.axel.trainingmetricsapi.training.infrastructure.persistence;

import com.axel.trainingmetricsapi.athlete.infrastructure.persistence.AthleteJpaEntity;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import org.springframework.stereotype.Component;

@Component
public class LoadReportPersistenceMapper {

    public LoadReportJpaEntity domainToEntity(LoadReport report) {
        AthleteJpaEntity athleteEntity = new AthleteJpaEntity(); // Phantom entity
        athleteEntity.setId(report.athleteId());                 // Hibernate only needs the id to persist the FK
        return new LoadReportJpaEntity(null, athleteEntity, report.weekStartDate(),
            report.totalFosterLoad(), report.sessionCount(), null);
    }

    public LoadReport entityToDomain(LoadReportJpaEntity entity) {
        return new LoadReport(
            entity.getAthlete().getId(),
            entity.getWeekStartDate(),
            entity.getTotalFosterLoad(),
            entity.getSessionCount(),
            entity.getUpdatedAt()
        );
    }
}
