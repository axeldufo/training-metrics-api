package com.axel.trainingmetricsapi.repository;

import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private AthleteJpaEntity athlete;
}
