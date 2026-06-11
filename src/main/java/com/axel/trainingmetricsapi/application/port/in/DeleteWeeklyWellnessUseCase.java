package com.axel.trainingmetricsapi.application.port.in;

public interface DeleteWeeklyWellnessUseCase {
    void execute(long wellnessId, long athleteId, long coachId);
}
