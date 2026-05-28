package com.axel.trainingmetricsapi.domain.event;

import java.time.LocalDate;

public record TrainingSessionUpdatedEvent(long athleteId, LocalDate date) {}
