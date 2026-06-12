package com.axel.trainingmetricsapi.identity.interfaces.web;

import com.axel.trainingmetricsapi.identity.application.port.in.LoginUseCase;
import com.axel.trainingmetricsapi.identity.application.port.in.RegisterCoachUseCase;
import com.axel.trainingmetricsapi.identity.domain.CoachAuthData;
import com.axel.trainingmetricsapi.identity.domain.CoachCredentials;
import com.axel.trainingmetricsapi.shared.interfaces.web.ApiConstants;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.LoginRequest;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.RegisterRequest;
import com.axel.trainingmetricsapi.shared.interfaces.web.dto.ApiError;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(ApiConstants.API_VERSION + "/auth")
@RestController
public class AuthController {

    private final RegisterCoachUseCase registerCoachUseCase;
    private final LoginUseCase loginUseCase;
    private final AuthWebMapper authWebMapper;

    public AuthController(RegisterCoachUseCase registerCoachUseCase,
                          LoginUseCase loginUseCase,
                          AuthWebMapper authWebMapper) {
        this.registerCoachUseCase = registerCoachUseCase;
        this.loginUseCase = loginUseCase;
        this.authWebMapper = authWebMapper;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new coach")
    @ApiResponse(responseCode = "201", description = "Coach created and registered", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest registerRequest) {
        CoachCredentials credentials = authWebMapper.toCredentials(registerRequest);
        CoachAuthData authData = registerCoachUseCase.execute(credentials);
        AuthResponse authResponse = authWebMapper.toAuthResponse(authData);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/login")
    @Operation(summary = "Coach authentication")
    @ApiResponse(responseCode = "200", description = "Coach authenticated", content = @Content(mediaType =
        "application/json", schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(mediaType =
        "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiError.class))))
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        CoachAuthData authData = loginUseCase.execute(loginRequest.email(), loginRequest.password());
        AuthResponse authResponse = authWebMapper.toAuthResponse(authData);
        return ResponseEntity.ok(authResponse);
    }
}
