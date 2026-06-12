package com.axel.trainingmetricsapi.training.application.port.in;

import java.time.LocalDate;

public interface TrainingSessionChangedUseCase {
    void execute(long athleteId, LocalDate date);
}
