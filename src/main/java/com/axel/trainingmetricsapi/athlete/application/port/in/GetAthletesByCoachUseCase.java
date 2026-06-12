package com.axel.trainingmetricsapi.athlete.application.port.in;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.shared.domain.PageResult;

public interface GetAthletesByCoachUseCase {
    PageResult<Athlete> execute(long coachId, int pageNumber, int pageSize);
}
