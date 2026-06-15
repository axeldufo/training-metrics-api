package com.axel.trainingmetricsapi.athlete.infrastructure.persistence;

import com.axel.trainingmetricsapi.shared.domain.Sport;
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

    @Column(name = "coach_id", nullable = false)
    private Long coachId;

    @Column(name = "weight_in_kg")
    private Double weightInKg;

}
