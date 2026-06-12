package com.axel.trainingmetricsapi.identity.domain;

import com.axel.trainingmetricsapi.shared.domain.exception.DomainValidationException;
import lombok.*;

@Getter
@EqualsAndHashCode(exclude = "id")
@ToString
public class Coach {
    @Setter private Long id;
    private final String name;
    private final String email;

    public Coach(String name, String email) {
        if (name == null || name.isBlank()) throw new DomainValidationException("Coach name is required");
        if (email == null || email.isBlank()) throw new DomainValidationException("Coach email is required");
        this.name = name;
        this.email = email;
    }
}
