package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.PageResult;

public interface GetAthletesByCoachUseCase {
    PageResult<Athlete> execute(long coachId, int pageNumber, int pageSize);
}
