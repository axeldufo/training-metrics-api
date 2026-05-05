package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.Coach;
import com.axel.trainingmetricsapi.dto.request.CoachRequest;
import com.axel.trainingmetricsapi.dto.response.CoachResponse;
import org.springframework.stereotype.Component;

@Component
public class CoachWebMapper {

    public CoachResponse domainToResponse(Coach coach) {
        return new CoachResponse(
            coach.getId(),
            coach.getName());
    }

    public Coach requestToDomain(CoachRequest coachRequest) {
        return new Coach(coachRequest.name());
    }
}
