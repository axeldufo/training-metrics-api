package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Athlete;

import java.util.List;

public interface AthleteService {
    List<Athlete> findAllByCoachId(long coachId);
    Athlete save(Athlete athlete);
    Athlete findById(long athleteId, long coachId);
    Athlete update(Athlete athlete);
    void deleteById(long athleteId, long coachId);
}
