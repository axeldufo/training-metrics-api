package com.axel.trainingmetricsapi.domain;

import lombok.*;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = "id")
@ToString
public class Athlete {
    @Setter private Long id;
    private final String firstName;
    private final String lastName;
    private final LocalDate birthDate;
    private final Sport sport;
    private final Double weightInKg;
}
