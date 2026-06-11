package com.axel.trainingmetricsapi.application.athlete;

import com.axel.trainingmetricsapi.application.port.in.DeleteAthleteUseCase;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DeleteAthleteUseCaseImpl implements DeleteAthleteUseCase {

    private final AthleteRepository athleteRepository;

    public DeleteAthleteUseCaseImpl(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    @Override
    @Transactional
    public void execute(long athleteId, long coachId) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        athleteRepository.deleteById(athleteId);
    }
}
