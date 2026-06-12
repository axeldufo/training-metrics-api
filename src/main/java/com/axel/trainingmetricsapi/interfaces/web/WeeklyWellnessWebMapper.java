package com.axel.trainingmetricsapi.interfaces.web;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.interfaces.web.dto.request.WeeklyWellnessRequest;
import com.axel.trainingmetricsapi.interfaces.web.dto.response.WeeklyWellnessResponse;
import org.springframework.stereotype.Component;

@Component
public class WeeklyWellnessWebMapper {

    public WeeklyWellness requestToDomain(WeeklyWellnessRequest request, long athleteId) {
        return new WeeklyWellness(
            athleteId,
            request.weekStartDate(),
            request.perceivedDifficulty(),
            request.perceivedFatigue(),
            request.motivation()
        );
    }

    public WeeklyWellnessResponse domainToResponse(WeeklyWellness wellness) {
        return new WeeklyWellnessResponse(
            wellness.getId(),
            wellness.getAthleteId(),
            wellness.getWeekStartDate(),
            wellness.getPerceivedDifficulty(),
            wellness.getPerceivedFatigue(),
            wellness.getMotivation()
        );
    }
}
