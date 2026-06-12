package com.axel.trainingmetricsapi.application.coach;

import com.axel.trainingmetricsapi.application.port.in.UpdateCoachUseCase;
import com.axel.trainingmetricsapi.domain.CoachRepository;
import com.axel.trainingmetricsapi.domain.exception.CoachNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UpdateCoachUseCaseImpl implements UpdateCoachUseCase {

    private final CoachRepository coachRepository;

    public UpdateCoachUseCaseImpl(CoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    @Override
    @Transactional
    public void execute(long coachId, String name) {
        if (!coachRepository.existsById(coachId)) {
            throw new CoachNotFoundException(coachId);
        }
        coachRepository.updateName(coachId, name);
    }
}
