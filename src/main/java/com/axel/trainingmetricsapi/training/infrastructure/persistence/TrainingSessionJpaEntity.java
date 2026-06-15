package com.axel.trainingmetricsapi.training.infrastructure.persistence;

import com.axel.trainingmetricsapi.shared.domain.Sport;
import com.axel.trainingmetricsapi.training.domain.TargetZone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "training_session")
@Getter
@Setter
@NoArgsConstructor  // for JPA
@AllArgsConstructor
public class TrainingSessionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport", nullable = false)
    private Sport sport;

    @Column(name = "rpe", nullable = false)
    private int rpe;

    @Column(name = "duration_in_min", nullable = false)
    private int durationInMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_zone", nullable = false, length = 2)
    private TargetZone targetZone;

    @Column(name = "athlete_id", nullable = false)
    private Long athleteId;

}
