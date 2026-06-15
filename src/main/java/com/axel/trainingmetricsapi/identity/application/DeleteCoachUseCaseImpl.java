package com.axel.trainingmetricsapi.identity.application;

import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.identity.application.port.in.DeleteCoachUseCase;
import com.axel.trainingmetricsapi.identity.domain.CoachRepository;
import com.axel.trainingmetricsapi.identity.domain.exception.CoachHasAthletesException;
import com.axel.trainingmetricsapi.identity.domain.exception.CoachNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DeleteCoachUseCaseImpl implements DeleteCoachUseCase {

    private final CoachRepository coachRepository;
    private final AthleteRepository athleteRepository;

    public DeleteCoachUseCaseImpl(CoachRepository coachRepository, AthleteRepository athleteRepository) {
        this.coachRepository = coachRepository;
        this.athleteRepository = athleteRepository;
    }

    @Override
    @Transactional
    public void execute(long coachId) {
        if (!coachRepository.existsById(coachId)) {
            throw new CoachNotFoundException(coachId);
        }
        if (athleteRepository.existsByCoachId(coachId)) {
            throw new CoachHasAthletesException(coachId);
        }
        coachRepository.deleteById(coachId);
    }
}
