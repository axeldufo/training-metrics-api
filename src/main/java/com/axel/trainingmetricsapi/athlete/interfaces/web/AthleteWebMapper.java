package com.axel.trainingmetricsapi.athlete.interfaces.web;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.interfaces.web.dto.AthleteRequest;
import com.axel.trainingmetricsapi.athlete.interfaces.web.dto.AthleteResponse;
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
