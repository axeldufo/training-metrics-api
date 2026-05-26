package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Coach;

public interface CoachService {
    Coach findById(long coachId);
    void deleteById(long coachId);
    Coach updateName(long id, String name);
}
