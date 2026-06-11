package com.axel.trainingmetricsapi.application.port.in;

public interface UpdateCoachUseCase {
    void execute(long coachId, String name);
}
