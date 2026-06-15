package com.axel.trainingmetricsapi.training.infrastructure.persistence;

import com.axel.trainingmetricsapi.training.domain.LoadReport;
import org.springframework.stereotype.Component;

@Component
public class LoadReportPersistenceMapper {

    public LoadReportJpaEntity domainToEntity(LoadReport report) {
        return new LoadReportJpaEntity(null, report.athleteId(), report.weekStartDate(),
            report.totalFosterLoad(), report.sessionCount(), null);
    }

    public LoadReport entityToDomain(LoadReportJpaEntity entity) {
        return new LoadReport(
            entity.getAthleteId(),
            entity.getWeekStartDate(),
            entity.getTotalFosterLoad(),
            entity.getSessionCount(),
            entity.getUpdatedAt()
        );
    }
}
