package com.axel.trainingmetricsapi.identity.application.port.in;

public interface UpdateCoachUseCase {
    void execute(long coachId, String name);
}
