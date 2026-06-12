package com.axel.trainingmetricsapi.athlete.domain;

import com.axel.trainingmetricsapi.shared.domain.Sport;
import com.axel.trainingmetricsapi.shared.domain.exception.DomainValidationException;
import lombok.*;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode(exclude = "id")
@ToString
public class Athlete {
    @Setter private Long id;
    private final String firstName;
    private final String lastName;
    private final LocalDate birthDate;
    private final Sport sport;
    private final Long coachId;
    private final Double weightInKg;

    public Athlete(String firstName, String lastName, LocalDate birthDate, Sport sport, Long coachId, Double weightInKg) {
        if (firstName == null || firstName.isBlank()) throw new DomainValidationException("Athlete first name is required");
        if (lastName == null || lastName.isBlank()) throw new DomainValidationException("Athlete last name is required");
        if (sport == null) throw new DomainValidationException("Athlete sport is required");
        if (coachId == null) throw new DomainValidationException("Athlete coach is required");
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.sport = sport;
        this.coachId = coachId;
        this.weightInKg = weightInKg;
    }
}
