package com.axel.trainingmetricsapi.wellness.infrastructure.persistence;

import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import org.springframework.stereotype.Component;

@Component
public class WeeklyWellnessPersistenceMapper {

    public WeeklyWellnessJpaEntity domainToEntity(WeeklyWellness wellness) {
        return new WeeklyWellnessJpaEntity(
            wellness.getId(),
            wellness.getWeekStartDate(),
            wellness.getPerceivedDifficulty(),
            wellness.getPerceivedFatigue(),
            wellness.getMotivation(),
            wellness.getAthleteId()
        );
    }

    public WeeklyWellness entityToDomain(WeeklyWellnessJpaEntity entity) {
        WeeklyWellness wellness = new WeeklyWellness(
            entity.getAthleteId(),
            entity.getWeekStartDate(),
            entity.getPerceivedDifficulty(),
            entity.getPerceivedFatigue(),
            entity.getMotivation()
        );
        wellness.setId(entity.getId());
        return wellness;
    }
}
