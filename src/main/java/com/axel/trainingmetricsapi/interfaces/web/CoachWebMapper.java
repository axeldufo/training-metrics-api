package com.axel.trainingmetricsapi.interfaces.web;

import com.axel.trainingmetricsapi.domain.Coach;
import com.axel.trainingmetricsapi.interfaces.web.dto.response.CoachResponse;
import org.springframework.stereotype.Component;

@Component
public class CoachWebMapper {

    public CoachResponse domainToResponse(Coach coach) {
        return new CoachResponse(
            coach.getId(),
            coach.getName());
    }

}
