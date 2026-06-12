package com.axel.trainingmetricsapi.wellness.infrastructure.persistence;

import com.axel.trainingmetricsapi.athlete.infrastructure.persistence.AthleteJpaEntity;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import org.springframework.stereotype.Component;

@Component
public class WeeklyWellnessPersistenceMapper {

    public WeeklyWellnessJpaEntity domainToEntity(WeeklyWellness wellness) {
        AthleteJpaEntity athleteEntity = new AthleteJpaEntity(); // Phantom entity
        athleteEntity.setId(wellness.getAthleteId());            // Hibernate only needs the id to persist the FK
        return new WeeklyWellnessJpaEntity(
            wellness.getId(),
            wellness.getWeekStartDate(),
            wellness.getPerceivedDifficulty(),
            wellness.getPerceivedFatigue(),
            wellness.getMotivation(),
            athleteEntity
        );
    }

    public WeeklyWellness entityToDomain(WeeklyWellnessJpaEntity entity) {
        WeeklyWellness wellness = new WeeklyWellness(
            entity.getAthlete().getId(),
            entity.getWeekStartDate(),
            entity.getPerceivedDifficulty(),
            entity.getPerceivedFatigue(),
            entity.getMotivation()
        );
        wellness.setId(entity.getId());
        return wellness;
    }
}
