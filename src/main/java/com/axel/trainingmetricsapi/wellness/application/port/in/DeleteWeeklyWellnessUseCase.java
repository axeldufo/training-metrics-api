package com.axel.trainingmetricsapi.wellness.application.port.in;

public interface DeleteWeeklyWellnessUseCase {
    void execute(long wellnessId, long athleteId, long coachId);
}
