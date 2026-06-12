package com.axel.trainingmetricsapi.infrastructure.persistence;

import com.axel.trainingmetricsapi.domain.Athlete;
import org.springframework.stereotype.Component;

@Component
public class AthletePersistenceMapper {

    public AthleteJpaEntity domainToEntity(Athlete athlete) {
        CoachJpaEntity coach = CoachJpaEntity.builder()
            .id(athlete.getCoachId())
            .build(); // Phantom entity — Hibernate only needs the id to persist the FK
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
        Long coachId = athleteJpaEntity.getCoach().getId();
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
