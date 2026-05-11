package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Coach;
import com.axel.trainingmetricsapi.domain.CoachRepository;
import com.axel.trainingmetricsapi.domain.exception.CoachNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CoachServiceImpl implements CoachService {

    private final CoachRepository coachRepository;

    public CoachServiceImpl(CoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    @Override
    public List<Coach> findAll() {
        return coachRepository.findAll();
    }

    @Override
    public Coach findById(long coachId) {
        return coachRepository.findById(coachId)
            .orElseThrow(() -> new CoachNotFoundException(coachId));
    }

    @Override
    @Transactional
    public Coach save(Coach coach) {
        return coachRepository.save(coach);
    }

    @Override
    @Transactional
    public Coach update(Coach coach) {
        Long coachId = coach.getId();
        if (!coachRepository.existsById(coachId)) {
            throw new CoachNotFoundException(coachId);
        }
        return coachRepository.save(coach);
    }

    @Override
    @Transactional
    public void deleteById(long coachId) {
        if (!coachRepository.existsById(coachId)) {
            throw new CoachNotFoundException(coachId);
        }
        coachRepository.deleteById(coachId);
    }
}
