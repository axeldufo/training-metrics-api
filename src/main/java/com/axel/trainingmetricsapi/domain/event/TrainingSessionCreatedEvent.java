package com.axel.trainingmetricsapi.domain.event;

import java.time.LocalDate;

public record TrainingSessionCreatedEvent(long athleteId, LocalDate date) {}
