package com.axel.trainingmetricsapi.mapper;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.dto.request.AthleteRequest;
import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import com.axel.trainingmetricsapi.repository.AthleteJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AthleteMapper {

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

    public AthleteResponse domainToResponse(Athlete athlete){
        return new AthleteResponse(
            athlete.getId(),
            athlete.getFirstName(),
            athlete.getLastName(),
            athlete.getBirthDate(),
            athlete.getSport(),
            athlete.getWeightInKg());
    }

    public Athlete requestToDomain(AthleteRequest athleteRequest) {
        return new Athlete(
            athleteRequest.firstName(),
            athleteRequest.lastName(),
            athleteRequest.birthDate(),
            athleteRequest.sport(),
            athleteRequest.weightInKg());
    }
}
