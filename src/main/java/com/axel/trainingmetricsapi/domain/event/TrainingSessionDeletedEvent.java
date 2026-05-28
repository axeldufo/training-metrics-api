package com.axel.trainingmetricsapi.domain.event;

import java.time.LocalDate;

public record TrainingSessionDeletedEvent(long athleteId, LocalDate date) {}
