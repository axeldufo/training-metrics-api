package com.axel.trainingmetricsapi.wellness.domain;

import com.axel.trainingmetricsapi.shared.domain.exception.DomainValidationException;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@EqualsAndHashCode(exclude = "id")
@ToString
public class WeeklyWellness {
    @Setter private Long id;
    private final long athleteId;
    private final LocalDate weekStartDate;
    private final int perceivedDifficulty;
    private final int perceivedFatigue;
    private final int motivation;

    public WeeklyWellness(Long athleteId, LocalDate weekStartDate, Integer perceivedDifficulty,
                          Integer perceivedFatigue, Integer motivation) {
        if (athleteId == null) throw new DomainValidationException("WeeklyWellness must be linked to an athlete");
        if (weekStartDate == null) throw new DomainValidationException("WeeklyWellness weekStartDate is required");
        if (!weekStartDate.getDayOfWeek().equals(DayOfWeek.MONDAY))
            throw new DomainValidationException("WeeklyWellness weekStartDate must be a Monday");
        if (perceivedDifficulty == null) throw new DomainValidationException("WeeklyWellness perceivedDifficulty is required");
        if (perceivedDifficulty < 1 || perceivedDifficulty > 5)
            throw new DomainValidationException("WeeklyWellness perceivedDifficulty must be between 1 and 5");
        if (perceivedFatigue == null) throw new DomainValidationException("WeeklyWellness perceivedFatigue is required");
        if (perceivedFatigue < 1 || perceivedFatigue > 5)
            throw new DomainValidationException("WeeklyWellness perceivedFatigue must be between 1 and 5");
        if (motivation == null) throw new DomainValidationException("WeeklyWellness motivation is required");
        if (motivation < 1 || motivation > 5)
            throw new DomainValidationException("WeeklyWellness motivation must be between 1 and 5");
        this.athleteId = athleteId;
        this.weekStartDate = weekStartDate;
        this.perceivedDifficulty = perceivedDifficulty;
        this.perceivedFatigue = perceivedFatigue;
        this.motivation = motivation;
    }
}
