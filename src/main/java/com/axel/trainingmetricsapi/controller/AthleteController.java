package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.dto.request.AthleteRequest;
import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import com.axel.trainingmetricsapi.mapper.AthleteMapper;
import com.axel.trainingmetricsapi.service.AthleteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
        return ResponseEntity.ok(athleteService.findAll().stream().map(athleteMapper::domainToResponse).toList());
    }

    @PostMapping
    public ResponseEntity<AthleteResponse> create(@RequestBody @Valid AthleteRequest athleteRequest) {
        Athlete athlete = athleteMapper.requestToDomain(athleteRequest);
        Athlete persistedAthlete = athleteService.save(athlete);
        AthleteResponse athleteResponse = athleteMapper.domainToResponse(persistedAthlete);
        URI location = URI.create("/athletes/" + persistedAthlete.getId());
        return ResponseEntity.created(location).body(athleteResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AthleteResponse> getById(@PathVariable Long id){
        Athlete athleteFound = athleteService.findById(id);
        AthleteResponse athleteResponse = athleteMapper.domainToResponse(athleteFound);
        return ResponseEntity.ok(athleteResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AthleteResponse> updateById(@PathVariable Long id,
                                                      @RequestBody @Valid AthleteRequest athleteRequest) {
        Athlete athleteToUpdate = athleteMapper.requestToDomain(athleteRequest);
        athleteToUpdate.setId(id);
        Athlete persistedAthlete = athleteService.update(athleteToUpdate);
        AthleteResponse athleteResponse = athleteMapper.domainToResponse(persistedAthlete);
        return ResponseEntity.ok(athleteResponse);
    }
}
