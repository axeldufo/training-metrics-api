package com.axel.trainingmetricsapi.domain;

import com.axel.trainingmetricsapi.domain.exception.DomainValidationException;
import lombok.*;

@Getter
@EqualsAndHashCode(exclude = "id")
@ToString
public class Coach {
    @Setter private Long id;
    private final String name;

    public Coach(String name) {
        if (name == null || name.isBlank()) throw new DomainValidationException("Coach name is required");
        this.name = name;
    }
}
