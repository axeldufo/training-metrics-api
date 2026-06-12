package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.application.port.in.CreateAthleteUseCase;
import com.axel.trainingmetricsapi.application.port.in.DeleteAthleteUseCase;
import com.axel.trainingmetricsapi.application.port.in.GetAthleteUseCase;
import com.axel.trainingmetricsapi.application.port.in.GetAthletesByCoachUseCase;
import com.axel.trainingmetricsapi.application.port.in.UpdateAthleteUseCase;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.PageResult;
import com.axel.trainingmetricsapi.dto.request.AthleteRequest;
import com.axel.trainingmetricsapi.dto.response.ApiError;
import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import com.axel.trainingmetricsapi.dto.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.axel.trainingmetricsapi.controller.ApiConstants.DEFAULT_PAGE;
import static com.axel.trainingmetricsapi.controller.ApiConstants.DEFAULT_SIZE;

@RequestMapping(ApiConstants.API_VERSION + "/athletes")
@RestController
public class AthleteController {

    private final AthleteWebMapper athleteWebMapper;
    private final GetAthletesByCoachUseCase getAthletesByCoachUseCase;
    private final CreateAthleteUseCase createAthleteUseCase;
    private final GetAthleteUseCase getAthleteUseCase;
    private final UpdateAthleteUseCase updateAthleteUseCase;
    private final DeleteAthleteUseCase deleteAthleteUseCase;
    private final AuthenticatedCoachResolver authenticatedCoachResolver;

    public AthleteController(AthleteWebMapper athleteWebMapper,
                              GetAthletesByCoachUseCase getAthletesByCoachUseCase,
                              CreateAthleteUseCase createAthleteUseCase,
                              GetAthleteUseCase getAthleteUseCase,
                              UpdateAthleteUseCase updateAthleteUseCase,
                              DeleteAthleteUseCase deleteAthleteUseCase,
                              AuthenticatedCoachResolver authenticatedCoachResolver) {
        this.athleteWebMapper = athleteWebMapper;
        this.getAthletesByCoachUseCase = getAthletesByCoachUseCase;
        this.createAthleteUseCase = createAthleteUseCase;
        this.getAthleteUseCase = getAthleteUseCase;
        this.updateAthleteUseCase = updateAthleteUseCase;
        this.deleteAthleteUseCase = deleteAthleteUseCase;
        this.authenticatedCoachResolver = authenticatedCoachResolver;
    }

    @GetMapping
    @Operation(summary = "Retrieve all athletes of the authenticated coach")
    @ApiResponse(responseCode = "200", description = "Paginated list of athletes (content, totalElements, page, size)." +
        "Content contains AthleteResponse objects.", content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = PagedResponse.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    public ResponseEntity<PagedResponse<AthleteResponse>> getAll(@RequestParam(defaultValue = DEFAULT_PAGE) int page,
                                                                 @RequestParam(defaultValue = DEFAULT_SIZE) int size) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        PageResult<Athlete> pageResult = getAthletesByCoachUseCase.execute(coachId, page, size);

        return ResponseEntity.ok(new PagedResponse<>(
            pageResult.content().stream().map(athleteWebMapper::domainToResponse).toList(),
            pageResult.totalElements(),
            pageResult.pageNumber(),
            pageResult.pageSize()));
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
        long coachId = coach.id();
        Athlete athlete = athleteWebMapper.requestToDomain(athleteRequest, coachId);
        Athlete persistedAthlete = createAthleteUseCase.execute(athlete);
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
        long coachId = coach.id();
        Athlete athleteFound = getAthleteUseCase.execute(id, coachId);
        return ResponseEntity.ok(athleteWebMapper.domainToResponse(athleteFound));
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
        long coachId = coach.id();
        Athlete athleteToUpdate = athleteWebMapper.requestToDomain(athleteRequest, coachId);
        athleteToUpdate.setId(id);
        Athlete persistedAthlete = updateAthleteUseCase.execute(athleteToUpdate, coachId);
        return ResponseEntity.ok(athleteWebMapper.domainToResponse(persistedAthlete));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete athlete by ID")
    @ApiResponse(responseCode = "204", description = "Athlete deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Athlete not found or does not belong to the authenticated coach",
        content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<Void> deleteById(@PathVariable long id) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        long coachId = coach.id();
        deleteAthleteUseCase.execute(id, coachId);
        return ResponseEntity.noContent().build();
    }
}
