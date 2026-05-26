package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.dto.request.AthleteRequest;
import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import org.springframework.stereotype.Component;

@Component
public class AthleteWebMapper {

    public AthleteResponse domainToResponse(Athlete athlete){
        return new AthleteResponse(
            athlete.getId(),
            athlete.getFirstName(),
            athlete.getLastName(),
            athlete.getBirthDate(),
            athlete.getSport(),
            athlete.getCoachId(),
            athlete.getWeightInKg());
    }

    public Athlete requestToDomain(AthleteRequest athleteRequest, long coachId) {
        return new Athlete(
            athleteRequest.firstName(),
            athleteRequest.lastName(),
            athleteRequest.birthDate(),
            athleteRequest.sport(),
            coachId,
            athleteRequest.weightInKg());
    }

}
