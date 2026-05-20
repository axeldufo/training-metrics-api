package com.axel.trainingmetricsapi.controller;

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

    public AthleteController(AthleteWebMapper athleteWebMapper, AthleteService athleteService) {
        this.athleteWebMapper = athleteWebMapper;
        this.athleteService = athleteService;
    }

    @GetMapping
    @Operation(summary = "Retrieve all athletes")
    @ApiResponse(responseCode = "200", description = "Athletes retrieved", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = AthleteResponse.class))))
    public ResponseEntity<List<AthleteResponse>> getAll() {
        return ResponseEntity.ok(athleteService.findAll().stream().map(athleteWebMapper::domainToResponse).toList());
    }

    @PostMapping
    @Operation(summary = "Create new athlete")
    @ApiResponse(responseCode = "201", description = "Athlete created", content = @Content(mediaType =
            "application/json", schema = @Schema(implementation = AthleteResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<AthleteResponse> create(@RequestBody @Valid AthleteRequest athleteRequest) {
        Athlete athlete = athleteWebMapper.requestToDomain(athleteRequest);
        Athlete persistedAthlete = athleteService.save(athlete);
        AthleteResponse athleteResponse = athleteWebMapper.domainToResponse(persistedAthlete);
        URI location = URI.create(ApiConstants.API_VERSION + "/athletes/" + athleteResponse.id());
        return ResponseEntity.created(location).body(athleteResponse);
    }

    @Operation(summary = "Retrieve athlete")
    @ApiResponse(responseCode = "200", description = "Athlete found and returned", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = AthleteResponse.class)))
    @ApiResponse(responseCode = "404", description = "Athlete not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @GetMapping("/{id}")
    public ResponseEntity<AthleteResponse> getById(@PathVariable long id){
        Athlete athleteFound = athleteService.findById(id);
        AthleteResponse athleteResponse = athleteWebMapper.domainToResponse(athleteFound);
        return ResponseEntity.ok(athleteResponse);
    }

    @Operation(summary = "Update athlete")
    @ApiResponse(responseCode = "200", description = "Athlete found and updated", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = AthleteResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "404", description = "Athlete not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @PutMapping("/{id}")
    public ResponseEntity<AthleteResponse> updateById(@PathVariable long id,
                                                      @RequestBody @Valid AthleteRequest athleteRequest) {
        Athlete athleteToUpdate = athleteWebMapper.requestToDomain(athleteRequest);
        athleteToUpdate.setId(id);
        Athlete persistedAthlete = athleteService.update(athleteToUpdate);
        AthleteResponse athleteResponse = athleteWebMapper.domainToResponse(persistedAthlete);
        return ResponseEntity.ok(athleteResponse);
    }

    @Operation(summary = "Delete athlete")
    @ApiResponse(responseCode = "204", description = "Athlete deleted")
    @ApiResponse(responseCode = "404", description = "Athlete not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable long id) {
        athleteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
