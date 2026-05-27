package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.PageResult;

public interface AthleteService {
    PageResult<Athlete> findAllByCoachId(long coachId, int pageNumber, int pageSize);
    Athlete save(Athlete athlete);
    Athlete findById(long athleteId, long coachId);
    Athlete update(Athlete athlete);
    void deleteById(long athleteId, long coachId);
}
