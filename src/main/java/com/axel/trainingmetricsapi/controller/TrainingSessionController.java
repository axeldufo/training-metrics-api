package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.dto.request.TrainingSessionRequest;
import com.axel.trainingmetricsapi.dto.response.ApiError;
import com.axel.trainingmetricsapi.dto.response.TrainingSessionResponse;
import com.axel.trainingmetricsapi.service.TrainingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequestMapping(path = ApiConstants.API_VERSION + "/athletes/{id}/sessions")
@RestController
public class TrainingSessionController {

    private final TrainingSessionWebMapper trainingSessionWebMapper;
    private final TrainingSessionService trainingSessionService;

    public TrainingSessionController(TrainingSessionWebMapper trainingSessionWebMapper,
                                     TrainingSessionService trainingSessionService) {
        this.trainingSessionWebMapper = trainingSessionWebMapper;
        this.trainingSessionService = trainingSessionService;
    }

    @PostMapping
    @Operation(summary = "Create new session")
    @ApiResponse(responseCode = "201", description = "Session created", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = TrainingSessionResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<TrainingSessionResponse> create(@PathVariable("id")  long athleteId,
        @RequestBody @Valid TrainingSessionRequest trainingSessionRequest) {
        TrainingSession trainingSession = trainingSessionWebMapper.requestToDomain(trainingSessionRequest, athleteId);
        TrainingSession persistedTrainingSession = trainingSessionService.save(trainingSession);
        TrainingSessionResponse trainingSessionResponse =
            trainingSessionWebMapper.domainToResponse(persistedTrainingSession);
        URI location = URI.create("/athletes/" + persistedTrainingSession.getAthleteId()
            + "/sessions/" + persistedTrainingSession.getId());
        return ResponseEntity.created(location).body(trainingSessionResponse);
    }

}
