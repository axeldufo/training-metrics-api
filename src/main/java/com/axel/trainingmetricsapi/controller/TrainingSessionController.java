package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.dto.request.TrainingSessionRequest;
import com.axel.trainingmetricsapi.dto.response.ApiError;
import com.axel.trainingmetricsapi.dto.response.ErrorCode;
import com.axel.trainingmetricsapi.dto.response.TrainingSessionResponse;
import com.axel.trainingmetricsapi.service.AthleteService;
import com.axel.trainingmetricsapi.service.TrainingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RequestMapping(path = ApiConstants.API_VERSION + "/athletes/{id}/sessions")
@RestController
public class TrainingSessionController {

    private final TrainingSessionWebMapper trainingSessionWebMapper;
    private final TrainingSessionService trainingSessionService;
    private final AuthenticatedCoachResolver authenticatedCoachResolver;
    private final AthleteService athleteService;

    public TrainingSessionController(TrainingSessionWebMapper trainingSessionWebMapper,
                                     TrainingSessionService trainingSessionService, AuthenticatedCoachResolver authenticatedCoachResolver, AthleteService athleteService) {
        this.trainingSessionWebMapper = trainingSessionWebMapper;
        this.trainingSessionService = trainingSessionService;
        this.authenticatedCoachResolver = authenticatedCoachResolver;
        this.athleteService = athleteService;
    }

    @PostMapping
    @Operation(summary = "Create new session")
    @ApiResponse(responseCode = "201", description = "Session created", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = TrainingSessionResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Athlete not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<TrainingSessionResponse> create(@PathVariable("id")  long athleteId,
        @RequestBody @Valid TrainingSessionRequest trainingSessionRequest) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.findById(athleteId, coach.id()); // validates Coach→Athlete ownership

        TrainingSession trainingSession = trainingSessionWebMapper.requestToDomain(trainingSessionRequest, athleteId);
        TrainingSession persistedTrainingSession = trainingSessionService.save(trainingSession);
        TrainingSessionResponse trainingSessionResponse =
            trainingSessionWebMapper.domainToResponse(persistedTrainingSession);

        URI location = URI.create(ApiConstants.API_VERSION + "/athletes/" + trainingSessionResponse.athleteId()
            + "/sessions/" + trainingSessionResponse.id());
        return ResponseEntity.created(location).body(trainingSessionResponse);
    }

    @GetMapping
    @Operation(summary = "Retrieve training sessions for a period")
    @ApiResponse(responseCode = "200", description = "List of training sessions in the period", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = TrainingSessionResponse.class))))
    @ApiResponse(responseCode = "400", description = "Invalid period parameters", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Athlete not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<List<?>> getByPeriod(
        @PathVariable("id") long athleteId,
        @Parameter(description = "Start date (inclusive), format YYYY-MM-DD", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @Parameter(description = "End date (inclusive), format YYYY-MM-DD, defaults to today")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.findById(athleteId, coach.id()); // validates Coach→Athlete ownership

        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        if (from.isAfter(effectiveTo)) {
            return ResponseEntity.badRequest().body(
                List.of(new ApiError(ErrorCode.HTTP_VALIDATION_ERROR, "to", "to must be after or equal to from")));
        }

        List<TrainingSession> sessions = trainingSessionService.findByAthleteIdAndPeriod(athleteId, from, effectiveTo);
        List<TrainingSessionResponse> responses = sessions.stream()
            .map(trainingSessionWebMapper::domainToResponse)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Retrieve training session")
    @ApiResponse(responseCode = "200", description = "Training session found and returned", content =
        @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingSessionResponse.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Training session not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<TrainingSessionResponse> getById(@PathVariable("id")  long athleteId,
                                                           @PathVariable long sessionId){
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.findById(athleteId, coach.id()); // validates Coach→Athlete ownership

        TrainingSession trainingSessionFound = trainingSessionService.findById(sessionId, athleteId);
        TrainingSessionResponse trainingSessionResponse = trainingSessionWebMapper.domainToResponse(trainingSessionFound);
        return ResponseEntity.ok(trainingSessionResponse);
    }

    @PutMapping("/{sessionId}")
    @Operation(summary = "Update training session")
    @ApiResponse(responseCode = "200", description = "Training session found and updated", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = TrainingSessionResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Training session not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<TrainingSessionResponse> updateById(@PathVariable("id")  long athleteId,
                                                              @PathVariable long sessionId,
                                                              @RequestBody @Valid TrainingSessionRequest trainingSessionRequest) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.findById(athleteId, coach.id()); // validates Coach→Athlete ownership

        TrainingSession trainingSessionToUpdate = trainingSessionWebMapper.requestToDomain(trainingSessionRequest, athleteId);
        trainingSessionToUpdate.setId(sessionId);
        TrainingSession persistedTrainingSession = trainingSessionService.update(trainingSessionToUpdate);
        TrainingSessionResponse trainingSessionResponse =
            trainingSessionWebMapper.domainToResponse(persistedTrainingSession);

        return ResponseEntity.ok(trainingSessionResponse);
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete training session")
    @ApiResponse(responseCode = "204", description = "Training session deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Training session not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<Void> deleteById(@PathVariable("id")  long athleteId, @PathVariable long sessionId) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.findById(athleteId, coach.id()); // validates Coach→Athlete ownership

        trainingSessionService.deleteById(sessionId, athleteId);

        return ResponseEntity.noContent().build();
    }

}
