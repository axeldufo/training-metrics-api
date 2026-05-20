package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Athlete;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AthletePersistenceMapper {

    public AthleteJpaEntity domainToEntity(Athlete athlete) {
        CoachJpaEntity coach = Optional.ofNullable(athlete.getCoachId()) // Phantom entity
            .map(id -> CoachJpaEntity.builder().id(id).build())          // Hibernate only needs the id to persist the FK
            .orElse(null);                                               // Coach may be null if not assigned
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
            .orElse(null);  // Coach id may be null if not assigned
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
