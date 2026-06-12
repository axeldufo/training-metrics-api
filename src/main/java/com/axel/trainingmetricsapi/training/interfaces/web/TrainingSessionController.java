package com.axel.trainingmetricsapi.training.interfaces.web;

import com.axel.trainingmetricsapi.training.application.port.in.CreateTrainingSessionUseCase;
import com.axel.trainingmetricsapi.training.application.port.in.DeleteTrainingSessionUseCase;
import com.axel.trainingmetricsapi.training.application.port.in.GetTrainingSessionUseCase;
import com.axel.trainingmetricsapi.training.application.port.in.GetTrainingSessionsByPeriodUseCase;
import com.axel.trainingmetricsapi.training.application.port.in.UpdateTrainingSessionUseCase;
import com.axel.trainingmetricsapi.identity.interfaces.web.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.identity.interfaces.web.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.TrainingSessionRequest;
import com.axel.trainingmetricsapi.shared.interfaces.web.dto.ApiError;
import com.axel.trainingmetricsapi.shared.interfaces.web.dto.ErrorCode;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.TrainingSessionResponse;
import com.axel.trainingmetricsapi.shared.interfaces.web.ApiConstants;
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
import java.util.Objects;

@RequestMapping(path = ApiConstants.API_VERSION + "/athletes/{id}/sessions")
@RestController
public class TrainingSessionController {

    private final TrainingSessionWebMapper trainingSessionWebMapper;
    private final CreateTrainingSessionUseCase createTrainingSessionUseCase;
    private final GetTrainingSessionUseCase getTrainingSessionUseCase;
    private final GetTrainingSessionsByPeriodUseCase getTrainingSessionsByPeriodUseCase;
    private final UpdateTrainingSessionUseCase updateTrainingSessionUseCase;
    private final DeleteTrainingSessionUseCase deleteTrainingSessionUseCase;
    private final AuthenticatedCoachResolver authenticatedCoachResolver;

    public TrainingSessionController(TrainingSessionWebMapper trainingSessionWebMapper,
                                     CreateTrainingSessionUseCase createTrainingSessionUseCase,
                                     GetTrainingSessionUseCase getTrainingSessionUseCase,
                                     GetTrainingSessionsByPeriodUseCase getTrainingSessionsByPeriodUseCase,
                                     UpdateTrainingSessionUseCase updateTrainingSessionUseCase,
                                     DeleteTrainingSessionUseCase deleteTrainingSessionUseCase,
                                     AuthenticatedCoachResolver authenticatedCoachResolver) {
        this.trainingSessionWebMapper = trainingSessionWebMapper;
        this.createTrainingSessionUseCase = createTrainingSessionUseCase;
        this.getTrainingSessionUseCase = getTrainingSessionUseCase;
        this.getTrainingSessionsByPeriodUseCase = getTrainingSessionsByPeriodUseCase;
        this.updateTrainingSessionUseCase = updateTrainingSessionUseCase;
        this.deleteTrainingSessionUseCase = deleteTrainingSessionUseCase;
        this.authenticatedCoachResolver = authenticatedCoachResolver;
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
    public ResponseEntity<TrainingSessionResponse> create(@PathVariable("id") long athleteId,
                                                          @RequestBody @Valid TrainingSessionRequest trainingSessionRequest) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        TrainingSession trainingSession = trainingSessionWebMapper.requestToDomain(trainingSessionRequest, athleteId);
        TrainingSession persistedTrainingSession = createTrainingSessionUseCase.execute(trainingSession, coachId);
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
        long coachId = coach.id();
        LocalDate effectiveTo = Objects.requireNonNullElseGet(to, LocalDate::now);
        if (from.isAfter(effectiveTo)) {
            return ResponseEntity.badRequest().body(
                List.of(new ApiError(ErrorCode.HTTP_VALIDATION_ERROR, "to", "to must be after or equal to from")));
        }

        List<TrainingSession> sessions = getTrainingSessionsByPeriodUseCase.execute(athleteId, coachId, from, effectiveTo);
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
    public ResponseEntity<TrainingSessionResponse> getById(@PathVariable("id") long athleteId,
                                                           @PathVariable long sessionId) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        TrainingSession trainingSessionFound = getTrainingSessionUseCase.execute(sessionId, athleteId, coachId);
        return ResponseEntity.ok(trainingSessionWebMapper.domainToResponse(trainingSessionFound));
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
    public ResponseEntity<TrainingSessionResponse> updateById(@PathVariable("id") long athleteId,
                                                              @PathVariable long sessionId,
                                                              @RequestBody @Valid TrainingSessionRequest trainingSessionRequest) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        TrainingSession trainingSessionToUpdate = trainingSessionWebMapper.requestToDomain(trainingSessionRequest, athleteId);
        trainingSessionToUpdate.setId(sessionId);
        TrainingSession persistedTrainingSession = updateTrainingSessionUseCase.execute(trainingSessionToUpdate, coachId);
        return ResponseEntity.ok(trainingSessionWebMapper.domainToResponse(persistedTrainingSession));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete training session")
    @ApiResponse(responseCode = "204", description = "Training session deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Training session not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<Void> deleteById(@PathVariable("id") long athleteId, @PathVariable long sessionId) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        deleteTrainingSessionUseCase.execute(sessionId, athleteId, coachId);
        return ResponseEntity.noContent().build();
    }
}
