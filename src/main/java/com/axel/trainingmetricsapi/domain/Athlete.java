package com.axel.trainingmetricsapi.domain;

import java.time.LocalDate;

public class Athlete {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Sport sport;
    private Double weightInKg;

    public Athlete(String firstName, String lastName, LocalDate birthDate, Sport sport, Double weightInKg) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.sport = sport;
        this.weightInKg = weightInKg;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public Sport getSport() {
        return sport;
    }

    public Double getWeightInKg() {
        return weightInKg;
    }

}
