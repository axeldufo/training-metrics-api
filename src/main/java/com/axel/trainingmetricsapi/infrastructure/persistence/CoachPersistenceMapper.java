package com.axel.trainingmetricsapi.infrastructure.persistence;

import com.axel.trainingmetricsapi.domain.Coach;
import org.springframework.stereotype.Component;

@Component
public class CoachPersistenceMapper {

    public Coach entityToDomain(CoachJpaEntity coachJpaEntity) {
        Coach coach = new Coach(coachJpaEntity.getName(), coachJpaEntity.getEmail());
        coach.setId(coachJpaEntity.getId());
        return coach;
    }

}
