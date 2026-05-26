package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Sport;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "athlete")
@Getter
@Setter
@NoArgsConstructor  // for JPA
@AllArgsConstructor
public class AthleteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport", nullable = false)
    private Sport sport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private CoachJpaEntity coach;

    @Column(name = "weight_in_kg")
    private Double weightInKg;

}
