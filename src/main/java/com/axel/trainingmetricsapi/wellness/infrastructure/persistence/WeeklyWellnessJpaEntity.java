package com.axel.trainingmetricsapi.wellness.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "weekly_wellness")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyWellnessJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "perceived_difficulty", nullable = false)
    private int perceivedDifficulty;

    @Column(name = "perceived_fatigue", nullable = false)
    private int perceivedFatigue;

    @Column(name = "motivation", nullable = false)
    private int motivation;

    @Column(name = "athlete_id", nullable = false)
    private Long athleteId;
}
