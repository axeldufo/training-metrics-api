package com.axel.trainingmetricsapi.dto.response;

import com.axel.trainingmetricsapi.domain.Sport;

import java.time.LocalDate;

public record AthleteResponse(
    Long id,
    String firstName,
    String lastName,
    LocalDate birthDate,
    Sport sport,
    Double weightInKg) {
}
