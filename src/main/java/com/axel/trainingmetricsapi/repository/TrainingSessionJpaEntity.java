package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Sport;
import com.axel.trainingmetricsapi.domain.TargetZone;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private AthleteJpaEntity athlete;

}
