package com.axel.trainingmetricsapi.training.domain;

import com.axel.trainingmetricsapi.shared.domain.exception.DomainValidationException;
import com.axel.trainingmetricsapi.shared.domain.Sport;
import lombok.*;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode(exclude = "id")
@ToString
public class TrainingSession {
    @Setter private Long id;
    private final LocalDate date;
    private final Sport sport;
    private final int rpe;
    private final int durationInMin;
    private final TargetZone targetZone;
    private final long athleteId;

    public TrainingSession(LocalDate date, Sport sport, Integer rpe, Integer durationInMin, TargetZone targetZone,
                           Long athleteId) {
        if (date == null) throw new DomainValidationException("Session date is required");
        if (sport == null) throw new DomainValidationException("Session sport is required");
        if (rpe == null) throw new DomainValidationException("Session RPE is required");
        if (rpe < 1 || rpe > 10) throw new DomainValidationException("Session RPE must be between 1 and 10");
        if (durationInMin == null) throw new DomainValidationException("Session duration is required");
        if (durationInMin < 1) throw new DomainValidationException("Session duration must be positive");
        if (targetZone == null) throw new DomainValidationException("Session must have a target zone");
        if (athleteId == null) throw new DomainValidationException("Session must be linked to an athlete");
        this.date = date;
        this.sport = sport;
        this.rpe = rpe;
        this.durationInMin = durationInMin;
        this.targetZone = targetZone;
        this.athleteId = athleteId;
    }

    public int getFosterLoad() {
        return rpe * durationInMin;
    }

    public boolean isAboveTargetZone() {
        return targetZone.isRpeTooHigh(rpe);
    }

    public boolean isBelowTargetZone() {
        return targetZone.isRpeTooLow(rpe);
    }
}
