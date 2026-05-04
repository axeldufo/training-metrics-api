package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Athlete;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AthletePersistenceMapper {

    public AthleteJpaEntity domainToEntity(Athlete athlete) {
        CoachJpaEntity coach = Optional.ofNullable(athlete.getCoachId())
            .map(id -> new CoachJpaEntity(id, null))
            .orElse(null);
        return new AthleteJpaEntity(
            athlete.getId(),
            athlete.getFirstName(),
            athlete.getLastName(),
            athlete.getBirthDate(),
            athlete.getSport(),
            coach,
            athlete.getWeightInKg());
    }

    public Athlete entityToDomain(AthleteJpaEntity athleteJpaEntity) {
        Long coachId = Optional.ofNullable(athleteJpaEntity.getCoach())
            .map(CoachJpaEntity::getId)
            .orElse(null);
        Athlete athlete = new Athlete(
            athleteJpaEntity.getFirstName(),
            athleteJpaEntity.getLastName(),
            athleteJpaEntity.getBirthDate(),
            athleteJpaEntity.getSport(),
            coachId,
            athleteJpaEntity.getWeightInKg());
        athlete.setId(athleteJpaEntity.getId());
        return athlete;
    }

}
