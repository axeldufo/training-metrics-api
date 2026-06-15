package com.axel.trainingmetricsapi.athlete.application;

import com.axel.trainingmetricsapi.athlete.application.port.in.CreateAthleteUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.identity.domain.CoachRepository;
import com.axel.trainingmetricsapi.identity.domain.exception.CoachNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CreateAthleteUseCaseImpl implements CreateAthleteUseCase {

    private final AthleteRepository athleteRepository;
    private final CoachRepository coachRepository;

    public CreateAthleteUseCaseImpl(AthleteRepository athleteRepository, CoachRepository coachRepository) {
        this.athleteRepository = athleteRepository;
        this.coachRepository = coachRepository;
    }

    @Override
    @Transactional
    public Athlete execute(Athlete athlete) {
        long coachId = athlete.getCoachId();
        if (!coachRepository.existsById(coachId)) {
            throw new CoachNotFoundException(coachId);
        }
        return athleteRepository.save(athlete);
    }
}
