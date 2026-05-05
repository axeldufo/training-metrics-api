package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Coach;
import java.util.List;

public interface CoachService {
    List<Coach> findAll();
    Coach findById(Long coachId);
    Coach save(Coach coach);
    Coach update(Coach coach);
    void deleteById(Long coachId);
}
