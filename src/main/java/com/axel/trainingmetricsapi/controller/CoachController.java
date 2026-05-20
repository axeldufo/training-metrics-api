package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.Coach;
import com.axel.trainingmetricsapi.dto.request.CoachUpdateRequest;
import com.axel.trainingmetricsapi.dto.response.ApiError;
import com.axel.trainingmetricsapi.dto.response.CoachResponse;
import com.axel.trainingmetricsapi.service.CoachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(ApiConstants.API_VERSION + "/coaches")
@RestController
public class CoachController {

    private final CoachWebMapper coachWebMapper;
    private final CoachService coachService;

    public CoachController(CoachWebMapper coachWebMapper, CoachService coachService) {
        this.coachWebMapper = coachWebMapper;
        this.coachService = coachService;
    }

    @GetMapping
    @Operation(summary = "Retrieve all coaches")
    @ApiResponse(responseCode = "200", description = "Coaches retrieved", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = CoachResponse.class))))
    public ResponseEntity<List<CoachResponse>> getAll() {
        return ResponseEntity.ok(coachService.findAll().stream().map(coachWebMapper::domainToResponse).toList());
    }

    @Operation(summary = "Retrieve coach")
    @ApiResponse(responseCode = "200", description = "Coach found and returned", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = CoachResponse.class)))
    @ApiResponse(responseCode = "404", description = "Coach not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @GetMapping("/{id}")
    public ResponseEntity<CoachResponse> getById(@PathVariable long id){
        Coach coachFound = coachService.findById(id);
        CoachResponse coachResponse = coachWebMapper.domainToResponse(coachFound);
        return ResponseEntity.ok(coachResponse);
    }

    @Operation(summary = "Update coach name")
    @ApiResponse(responseCode = "200", description = "Coach found and updated", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = CoachResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "404", description = "Coach not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @PutMapping("/{id}")
    public ResponseEntity<CoachResponse> updateById(@PathVariable long id,
                                                      @RequestBody @Valid CoachUpdateRequest coachUpdateRequest) {
        Coach persistedCoach = coachService.updateName(id, coachUpdateRequest.name());
        CoachResponse coachResponse = coachWebMapper.domainToResponse(persistedCoach);
        return ResponseEntity.ok(coachResponse);
    }

    @Operation(summary = "Delete coach")
    @ApiResponse(responseCode = "204", description = "Coach deleted")
    @ApiResponse(responseCode = "404", description = "Coach not found", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable long id) {
        coachService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
