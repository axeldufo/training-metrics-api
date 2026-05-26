package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.dto.request.AthleteRequest;
import com.axel.trainingmetricsapi.dto.response.ApiError;
import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import com.axel.trainingmetricsapi.service.AthleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequestMapping(ApiConstants.API_VERSION + "/athletes")
@RestController
public class AthleteController {

    private final AthleteWebMapper athleteWebMapper;
    private final AthleteService athleteService;
    private final AuthenticatedCoachResolver authenticatedCoachResolver;

    public AthleteController(AthleteWebMapper athleteWebMapper, AthleteService athleteService, AuthenticatedCoachResolver authenticatedCoachResolver) {
        this.athleteWebMapper = athleteWebMapper;
        this.athleteService = athleteService;
        this.authenticatedCoachResolver = authenticatedCoachResolver;
    }

    @GetMapping
    @Operation(summary = "Retrieve all athletes of the authenticated coach")
    @ApiResponse(responseCode = "200", description = "Athletes retrieved for the authenticated coach",
        content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation =
            AthleteResponse.class))))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    public ResponseEntity<List<AthleteResponse>> getAll() {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        return ResponseEntity.ok(athleteService.findAllByCoachId(coach.id()).stream()
            .map(athleteWebMapper::domainToResponse)
            .toList());
    }

    @PostMapping
    @Operation(summary = "Create a new athlete for the authenticated coach")
    @ApiResponse(responseCode = "201", description = "Athlete successfully created and linked to the authenticated coach",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AthleteResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    public ResponseEntity<AthleteResponse> create(@RequestBody @Valid AthleteRequest athleteRequest) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        Athlete athlete = athleteWebMapper.requestToDomain(athleteRequest, coach.id());
        Athlete persistedAthlete = athleteService.save(athlete);
        AthleteResponse athleteResponse = athleteWebMapper.domainToResponse(persistedAthlete);
        URI location = URI.create(ApiConstants.API_VERSION + "/athletes/" + athleteResponse.id());
        return ResponseEntity.created(location).body(athleteResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve athlete by ID")
    @ApiResponse(responseCode = "200", description = "Athlete found and returned", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = AthleteResponse.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Athlete not found or does not belong to the authenticated coach",
        content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<AthleteResponse> getById(@PathVariable long id) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        Athlete athleteFound = athleteService.findById(id, coach.id());
        AthleteResponse athleteResponse = athleteWebMapper.domainToResponse(athleteFound);
        return ResponseEntity.ok(athleteResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update athlete by ID")
    @ApiResponse(responseCode = "200", description = "Athlete found and updated", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = AthleteResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Athlete not found or does not belong to the authenticated coach",
        content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<AthleteResponse> updateById(@PathVariable long id,
                                                      @RequestBody @Valid AthleteRequest athleteRequest) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        Athlete athleteToUpdate = athleteWebMapper.requestToDomain(athleteRequest, coach.id());
        athleteToUpdate.setId(id);
        Athlete persistedAthlete = athleteService.update(athleteToUpdate);
        AthleteResponse athleteResponse = athleteWebMapper.domainToResponse(persistedAthlete);
        return ResponseEntity.ok(athleteResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete athlete by ID")
    @ApiResponse(responseCode = "204", description = "Athlete deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Athlete not found or does not belong to the authenticated coach",
        content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<Void> deleteById(@PathVariable long id) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.deleteById(id, coach.id());
        return ResponseEntity.noContent().build();
    }
}
