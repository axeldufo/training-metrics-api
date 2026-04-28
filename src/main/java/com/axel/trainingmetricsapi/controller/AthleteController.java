package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.dto.request.AthleteRequest;
import com.axel.trainingmetricsapi.dto.response.ApiError;
import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import com.axel.trainingmetricsapi.mapper.AthleteMapper;
import com.axel.trainingmetricsapi.service.AthleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Retrieve all athletes")
    @ApiResponse(responseCode = "200", description = "Athletes retrieved", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = AthleteResponse.class))))
    public ResponseEntity<List<AthleteResponse>> getAll() {
        return ResponseEntity.ok(athleteService.findAll().stream().map(athleteMapper::domainToResponse).toList());
    }

    @PostMapping
    @Operation(summary = "Create new athlete")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Athlete created", content = @Content(mediaType =
            "application/json", schema = @Schema(implementation = AthleteResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body", content =
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation =
                ApiError.class))))
    })
    public ResponseEntity<AthleteResponse> create(@RequestBody @Valid AthleteRequest athleteRequest) {
        Athlete athlete = athleteMapper.requestToDomain(athleteRequest);
        Athlete persistedAthlete = athleteService.save(athlete);
        AthleteResponse athleteResponse = athleteMapper.domainToResponse(persistedAthlete);
        URI location = URI.create("/athletes/" + persistedAthlete.getId());
        return ResponseEntity.created(location).body(athleteResponse);
    }

    @Operation(summary = "Retrieve athlete")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Athlete found and returned", content = @Content(mediaType =
            "application/json", schema = @Schema(implementation = AthleteResponse.class))),
        @ApiResponse(responseCode = "404", description = "Athlete not found", content =
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation =
                ApiError.class))))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AthleteResponse> getById(@PathVariable Long id){
        Athlete athleteFound = athleteService.findById(id);
        AthleteResponse athleteResponse = athleteMapper.domainToResponse(athleteFound);
        return ResponseEntity.ok(athleteResponse);
    }

    @Operation(summary = "Update athlete")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Athlete found and updated", content = @Content(mediaType =
            "application/json", schema = @Schema(implementation = AthleteResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body", content =
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation =
                ApiError.class)))),
        @ApiResponse(responseCode = "404", description = "Athlete not found", content =
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation =
                ApiError.class))))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AthleteResponse> updateById(@PathVariable Long id,
                                                      @RequestBody @Valid AthleteRequest athleteRequest) {
        Athlete athleteToUpdate = athleteMapper.requestToDomain(athleteRequest);
        athleteToUpdate.setId(id);
        Athlete persistedAthlete = athleteService.update(athleteToUpdate);
        AthleteResponse athleteResponse = athleteMapper.domainToResponse(persistedAthlete);
        return ResponseEntity.ok(athleteResponse);
    }

    @Operation(summary = "Delete athlete")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Athlete deleted"),
        @ApiResponse(responseCode = "404", description = "Athlete not found", content =
        @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation =
            ApiError.class))))
    })
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        athleteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
