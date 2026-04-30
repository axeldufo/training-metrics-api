package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Athlete;
import org.springframework.stereotype.Component;

@Component
public class AthletePersistenceMapper {

    public AthleteJpaEntity domainToEntity(Athlete athlete) {
        return new AthleteJpaEntity(
            athlete.getId(),
            athlete.getFirstName(),
            athlete.getLastName(),
            athlete.getBirthDate(),
            athlete.getSport(),
            athlete.getWeightInKg());
    }

    public Athlete entityToDomain(AthleteJpaEntity athleteJpaEntity) {
        Athlete athlete = new Athlete(
            athleteJpaEntity.getFirstName(),
            athleteJpaEntity.getLastName(),
            athleteJpaEntity.getBirthDate(),
            athleteJpaEntity.getSport(),
            athleteJpaEntity.getWeightInKg());
        athlete.setId(athleteJpaEntity.getId());
        return athlete;
    }

}
