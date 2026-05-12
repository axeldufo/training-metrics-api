package com.axel.trainingmetricsapi.dto.response;

import com.axel.trainingmetricsapi.domain.Sport;

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
