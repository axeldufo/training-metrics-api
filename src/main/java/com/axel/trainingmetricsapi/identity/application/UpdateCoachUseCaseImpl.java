package com.axel.trainingmetricsapi.identity.application;

import com.axel.trainingmetricsapi.identity.application.port.in.UpdateCoachUseCase;
import com.axel.trainingmetricsapi.identity.domain.CoachRepository;
import com.axel.trainingmetricsapi.identity.domain.exception.CoachNotFoundException;
import org.springframework.stereotype.Service;
import io.micrometer.observation.annotation.Observed;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCoachUseCaseImpl implements UpdateCoachUseCase {

    private final CoachRepository coachRepository;

    public UpdateCoachUseCaseImpl(CoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    @Override
    @Transactional
    @Observed(name = "use-case.update-coach")
    public void execute(long coachId, String name) {
        if (!coachRepository.existsById(coachId)) {
            throw new CoachNotFoundException(coachId);
        }
        coachRepository.updateName(coachId, name);
    }
}
