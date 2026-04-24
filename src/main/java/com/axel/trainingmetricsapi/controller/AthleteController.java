package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import com.axel.trainingmetricsapi.mapper.AthleteMapper;
import com.axel.trainingmetricsapi.service.AthleteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/athletes")
@RestController
public class AthleteController {

    private final AthleteMapper athleteMapper;
    private final AthleteService athleteService;

    public AthleteController(AthleteMapper athleteMapper, AthleteService athleteService) {
        this.athleteMapper = athleteMapper;
        this.athleteService = athleteService;
    }

    @GetMapping
    public ResponseEntity<List<AthleteResponse>> getAll() {
        return ResponseEntity.ok(athleteService.findAll().stream().map(athleteMapper::toResponse).toList());
    }
}
