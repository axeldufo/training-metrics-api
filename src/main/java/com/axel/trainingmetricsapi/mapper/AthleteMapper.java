package com.axel.trainingmetricsapi.mapper;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.repository.AthleteJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AthleteMapper {

    public AthleteJpaEntity toEntity(Athlete athlete) {
        AthleteJpaEntity athleteJpaEntity = new AthleteJpaEntity(
            athlete.getFirstName(),
            athlete.getLastName(),
            athlete.getBirthDate(),
            athlete.getSport(),
            athlete.getWeightInKg());
        athleteJpaEntity.setId(athlete.getId());
        return athleteJpaEntity;
    }

    public Athlete toDomain(AthleteJpaEntity athleteJpaEntity) {
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
