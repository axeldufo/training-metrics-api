package com.axel.trainingmetricsapi.athlete.application;

import com.axel.trainingmetricsapi.athlete.application.port.in.UpdateAthleteUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UpdateAthleteUseCaseImpl implements UpdateAthleteUseCase {

    private final AthleteRepository athleteRepository;

    public UpdateAthleteUseCaseImpl(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    @Override
    @Transactional
    public Athlete execute(Athlete athlete, long coachId) {
        long athleteId = athlete.getId();
        Athlete existing = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (existing.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        return athleteRepository.save(athlete);
    }
}
