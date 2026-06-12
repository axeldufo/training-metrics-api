package com.axel.trainingmetricsapi.athlete.interfaces.web.dto;

import com.axel.trainingmetricsapi.shared.domain.Sport;

import java.time.LocalDate;

public record AthleteResponse(
    long id,
    String firstName,
    String lastName,
    LocalDate birthDate,
    Sport sport,
    Long coachId,
    Double weightInKg) {
}
