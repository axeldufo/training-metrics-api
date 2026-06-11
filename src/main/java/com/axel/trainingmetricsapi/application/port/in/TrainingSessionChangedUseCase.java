package com.axel.trainingmetricsapi.application.port.in;

import java.time.LocalDate;

public interface TrainingSessionChangedUseCase {
    void execute(long athleteId, LocalDate date);
}
