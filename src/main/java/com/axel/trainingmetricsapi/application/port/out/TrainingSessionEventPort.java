package com.axel.trainingmetricsapi.application.port.out;

import java.time.LocalDate;

public interface TrainingSessionEventPort {
    void sessionCreated(long athleteId, LocalDate date);
    void sessionUpdated(long athleteId, LocalDate date);
    void sessionDeleted(long athleteId, LocalDate date);
}
