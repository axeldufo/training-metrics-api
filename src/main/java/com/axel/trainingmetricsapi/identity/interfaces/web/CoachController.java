package com.axel.trainingmetricsapi.identity.interfaces.web;

import com.axel.trainingmetricsapi.identity.application.port.in.DeleteCoachUseCase;
import com.axel.trainingmetricsapi.identity.application.port.in.GetCoachUseCase;
import com.axel.trainingmetricsapi.identity.application.port.in.UpdateCoachUseCase;
import com.axel.trainingmetricsapi.shared.interfaces.web.ApiConstants;
import com.axel.trainingmetricsapi.identity.interfaces.web.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.identity.interfaces.web.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.identity.domain.Coach;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.CoachUpdateRequest;
import com.axel.trainingmetricsapi.shared.interfaces.web.dto.ApiError;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.CoachResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiConstants.API_VERSION + "/coaches/me")
@RestController
public class CoachController {

    private final CoachWebMapper coachWebMapper;
    private final GetCoachUseCase getCoachUseCase;
    private final UpdateCoachUseCase updateCoachUseCase;
    private final DeleteCoachUseCase deleteCoachUseCase;
    private final AuthenticatedCoachResolver authenticatedCoachResolver;

    public CoachController(CoachWebMapper coachWebMapper,
                           GetCoachUseCase getCoachUseCase,
                           UpdateCoachUseCase updateCoachUseCase,
                           DeleteCoachUseCase deleteCoachUseCase,
                           AuthenticatedCoachResolver authenticatedCoachResolver) {
        this.coachWebMapper = coachWebMapper;
        this.getCoachUseCase = getCoachUseCase;
        this.updateCoachUseCase = updateCoachUseCase;
        this.deleteCoachUseCase = deleteCoachUseCase;
        this.authenticatedCoachResolver = authenticatedCoachResolver;
    }

    @GetMapping()
    @Operation(summary = "Retrieve coach information")
    @ApiResponse(responseCode = "200", description = "Coach found and returned", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = CoachResponse.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Coach not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<CoachResponse> getMe() {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        Coach coachFound = getCoachUseCase.execute(coachId);
        return ResponseEntity.ok(coachWebMapper.domainToResponse(coachFound));
    }

    @PutMapping()
    @Operation(summary = "Update coach name")
    @ApiResponse(responseCode = "200", description = "Coach found and updated", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = CoachResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Coach not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<CoachResponse> update(@RequestBody @Valid CoachUpdateRequest coachUpdateRequest) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        updateCoachUseCase.execute(coachId, coachUpdateRequest.name());
        Coach persistedCoach = getCoachUseCase.execute(coachId);
        return ResponseEntity.ok(coachWebMapper.domainToResponse(persistedCoach));
    }

    @DeleteMapping()
    @Operation(summary = "Delete coach")
    @ApiResponse(responseCode = "204", description = "Coach deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Coach not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<Void> delete() {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        deleteCoachUseCase.execute(coachId);
        return ResponseEntity.noContent().build();
    }
}
