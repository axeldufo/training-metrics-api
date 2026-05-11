package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Coach;
import java.util.List;

public interface CoachService {
    List<Coach> findAll();
    Coach findById(long coachId);
    Coach save(Coach coach);
    Coach update(Coach coach);
    void deleteById(long coachId);
}
