package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Coach;
import org.springframework.stereotype.Component;

@Component
public class CoachPersistenceMapper {

    public CoachJpaEntity domainToEntity(Coach coach) {
        return new CoachJpaEntity(
            coach.getId(),
            coach.getName());
    }

    public Coach entityToDomain(CoachJpaEntity coachJpaEntity) {
        Coach coach = new Coach(coachJpaEntity.getName());
        coach.setId(coachJpaEntity.getId());
        return coach;
    }

}
