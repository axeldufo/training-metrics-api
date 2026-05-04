package com.axel.trainingmetricsapi.domain;

import lombok.*;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = "id")
@ToString
public class Coach {
    @Setter private Long id;
    private final String name;
}
