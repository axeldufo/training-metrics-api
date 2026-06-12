package com.axel.trainingmetricsapi.identity.interfaces.web;

import com.axel.trainingmetricsapi.identity.domain.Coach;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.CoachResponse;
import org.springframework.stereotype.Component;

@Component
public class CoachWebMapper {

    public CoachResponse domainToResponse(Coach coach) {
        return new CoachResponse(
            coach.getId(),
            coach.getName());
    }

}
