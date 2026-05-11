package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Athlete;

import java.util.List;

public interface AthleteService {
    List<Athlete> findAll();
    Athlete save(Athlete athlete);
    Athlete findById(long athleteId);
    Athlete update(Athlete athlete);
    void deleteById(long athleteId);
}
