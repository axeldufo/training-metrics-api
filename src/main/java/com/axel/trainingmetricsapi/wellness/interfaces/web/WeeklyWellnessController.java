package com.axel.trainingmetricsapi.wellness.interfaces.web;

import com.axel.trainingmetricsapi.wellness.application.port.in.CreateWeeklyWellnessUseCase;
import com.axel.trainingmetricsapi.wellness.application.port.in.DeleteWeeklyWellnessUseCase;
import com.axel.trainingmetricsapi.wellness.application.port.in.GetWeeklyWellnessUseCase;
import com.axel.trainingmetricsapi.wellness.application.port.in.GetWeeklyWellnessesByPeriodUseCase;
import com.axel.trainingmetricsapi.wellness.application.port.in.UpdateWeeklyWellnessUseCase;
import com.axel.trainingmetricsapi.shared.interfaces.web.ApiConstants;
import com.axel.trainingmetricsapi.shared.interfaces.web.exception.InvalidPeriodException;
import com.axel.trainingmetricsapi.identity.interfaces.web.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.identity.interfaces.web.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.wellness.interfaces.web.dto.WeeklyWellnessRequest;
import com.axel.trainingmetricsapi.shared.interfaces.web.dto.ApiError;
import com.axel.trainingmetricsapi.wellness.interfaces.web.dto.WeeklyWellnessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Validated
@RequestMapping(path = ApiConstants.API_VERSION + "/athletes/{id}/wellness")
@RestController
public class WeeklyWellnessController {

    private final WeeklyWellnessWebMapper wellnessWebMapper;
    private final CreateWeeklyWellnessUseCase createWeeklyWellnessUseCase;
    private final GetWeeklyWellnessUseCase getWeeklyWellnessUseCase;
    private final GetWeeklyWellnessesByPeriodUseCase getWeeklyWellnessesByPeriodUseCase;
    private final UpdateWeeklyWellnessUseCase updateWeeklyWellnessUseCase;
    private final DeleteWeeklyWellnessUseCase deleteWeeklyWellnessUseCase;
    private final AuthenticatedCoachResolver authenticatedCoachResolver;

    public WeeklyWellnessController(WeeklyWellnessWebMapper wellnessWebMapper,
                                    CreateWeeklyWellnessUseCase createWeeklyWellnessUseCase,
                                    GetWeeklyWellnessUseCase getWeeklyWellnessUseCase,
                                    GetWeeklyWellnessesByPeriodUseCase getWeeklyWellnessesByPeriodUseCase,
                                    UpdateWeeklyWellnessUseCase updateWeeklyWellnessUseCase,
                                    DeleteWeeklyWellnessUseCase deleteWeeklyWellnessUseCase,
                                    AuthenticatedCoachResolver authenticatedCoachResolver) {
        this.wellnessWebMapper = wellnessWebMapper;
        this.createWeeklyWellnessUseCase = createWeeklyWellnessUseCase;
        this.getWeeklyWellnessUseCase = getWeeklyWellnessUseCase;
        this.getWeeklyWellnessesByPeriodUseCase = getWeeklyWellnessesByPeriodUseCase;
        this.updateWeeklyWellnessUseCase = updateWeeklyWellnessUseCase;
        this.deleteWeeklyWellnessUseCase = deleteWeeklyWellnessUseCase;
        this.authenticatedCoachResolver = authenticatedCoachResolver;
    }

    @PostMapping
    @Operation(summary = "Create weekly wellness entry")
    @ApiResponse(responseCode = "201", description = "Wellness entry created", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = WeeklyWellnessResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Athlete not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "409", description = "Wellness entry already exists for this week", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<WeeklyWellnessResponse> create(@PathVariable("id") long athleteId,
                                                         @RequestBody @Valid WeeklyWellnessRequest request) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        WeeklyWellness wellness = wellnessWebMapper.requestToDomain(request, athleteId);
        WeeklyWellness persisted = createWeeklyWellnessUseCase.execute(wellness, coachId);
        WeeklyWellnessResponse response = wellnessWebMapper.domainToResponse(persisted);
        URI location = URI.create(ApiConstants.API_VERSION + "/athletes/" + response.athleteId()
            + "/wellness/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Retrieve wellness entries for a period")
    @ApiResponse(responseCode = "200", description = "List of wellness entries in the period", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = WeeklyWellnessResponse.class))))
    @ApiResponse(responseCode = "400", description = "Invalid period parameters", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Athlete not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<List<WeeklyWellnessResponse>> getByPeriod(
        @PathVariable("id") long athleteId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PastOrPresent LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PastOrPresent LocalDate to) {

        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        LocalDate effectiveTo = Objects.requireNonNullElseGet(to, LocalDate::now);
        if (from.isAfter(effectiveTo)) {
            throw new InvalidPeriodException("from must be before or equal to to");
        }

        List<WeeklyWellness> wellnessList = getWeeklyWellnessesByPeriodUseCase.execute(athleteId, coachId, from, effectiveTo);
        List<WeeklyWellnessResponse> responses = wellnessList.stream()
            .map(wellnessWebMapper::domainToResponse)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{wellnessId}")
    @Operation(summary = "Retrieve a wellness entry")
    @ApiResponse(responseCode = "200", description = "Wellness entry found and returned", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = WeeklyWellnessResponse.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Wellness entry not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<WeeklyWellnessResponse> getById(@PathVariable("id") long athleteId,
                                                          @PathVariable long wellnessId) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        WeeklyWellness wellness = getWeeklyWellnessUseCase.execute(wellnessId, athleteId, coachId);
        return ResponseEntity.ok(wellnessWebMapper.domainToResponse(wellness));
    }

    @PutMapping("/{wellnessId}")
    @Operation(summary = "Update a wellness entry")
    @ApiResponse(responseCode = "200", description = "Wellness entry updated", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = WeeklyWellnessResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Wellness entry not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "409", description = "Wellness entry already exists for the target week", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<WeeklyWellnessResponse> updateById(@PathVariable("id") long athleteId,
                                                             @PathVariable long wellnessId,
                                                             @RequestBody @Valid WeeklyWellnessRequest request) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        WeeklyWellness wellnessToUpdate = wellnessWebMapper.requestToDomain(request, athleteId);
        wellnessToUpdate.setId(wellnessId);
        WeeklyWellness updated = updateWeeklyWellnessUseCase.execute(wellnessToUpdate, coachId);
        return ResponseEntity.ok(wellnessWebMapper.domainToResponse(updated));
    }

    @DeleteMapping("/{wellnessId}")
    @Operation(summary = "Delete a wellness entry")
    @ApiResponse(responseCode = "204", description = "Wellness entry deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Wellness entry not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<Void> deleteById(@PathVariable("id") long athleteId, @PathVariable long wellnessId) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        deleteWeeklyWellnessUseCase.execute(wellnessId, athleteId, coachId);
        return ResponseEntity.noContent().build();
    }
}
