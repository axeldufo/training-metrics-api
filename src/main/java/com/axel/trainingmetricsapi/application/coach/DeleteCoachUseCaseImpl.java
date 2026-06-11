package com.axel.trainingmetricsapi.application.coach;

import com.axel.trainingmetricsapi.application.port.in.DeleteCoachUseCase;
import com.axel.trainingmetricsapi.domain.CoachRepository;
import com.axel.trainingmetricsapi.domain.exception.CoachNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DeleteCoachUseCaseImpl implements DeleteCoachUseCase {

    private final CoachRepository coachRepository;

    public DeleteCoachUseCaseImpl(CoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    @Override
    @Transactional
    public void execute(long coachId) {
        if (!coachRepository.existsById(coachId)) {
            throw new CoachNotFoundException(coachId);
        }
        coachRepository.deleteById(coachId);
    }
}
