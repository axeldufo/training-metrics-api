package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Coach;

import java.util.List;

public interface CoachService {
    List<Coach> findAll();
    Coach findById(long coachId);
    void deleteById(long coachId);
    Coach updateName(long id, String name);
}
