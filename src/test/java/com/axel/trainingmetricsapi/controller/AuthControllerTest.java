package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.controller.security.JwtUtils;
import com.axel.trainingmetricsapi.domain.CoachAuthData;
import com.axel.trainingmetricsapi.domain.CoachCredentials;
import com.axel.trainingmetricsapi.domain.exception.EmailAlreadyExistsException;
import com.axel.trainingmetricsapi.domain.exception.InvalidCredentialsException;
import com.axel.trainingmetricsapi.dto.request.LoginRequest;
import com.axel.trainingmetricsapi.dto.request.RegisterRequest;
import com.axel.trainingmetricsapi.dto.response.AuthResponse;
import com.axel.trainingmetricsapi.service.AuthService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    static final String URL_PREFIX = ApiConstants.API_VERSION + "/auth";

    @MockitoBean
    private AuthWebMapper authWebMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean                 // Required by @WebMvcTest context — JwtAuthenticationFilter depends on JwtUtils
    protected JwtUtils jwtUtils; // Not used in assertions — auth endpoints are public

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_shouldReturnAuthResponse_whenCoachDoesntExists() throws Exception {
        RegisterRequest registerRequest = aValidRegisterRequest();
        CoachCredentials credentials = Instancio.create(CoachCredentials.class);
        when(authWebMapper.toCredentials(registerRequest)).thenReturn(credentials);
        CoachAuthData authData = Instancio.create(CoachAuthData.class);
        when(authService.register(credentials)).thenReturn(authData);
        AuthResponse authResponse = Instancio.create(AuthResponse.class);
        when(authWebMapper.toAuthResponse(authData)).thenReturn(authResponse);

        mvc.perform(post(URL_PREFIX + "/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").value(authResponse.token()));

        verify(authWebMapper).toCredentials(registerRequest);
        verify(authService).register(credentials);
        verify(authWebMapper).toAuthResponse(authData);
    }

    @Test
    void register_shouldReturnResourceConflict_whenEmailAlreadyExists() throws Exception {
        RegisterRequest registerRequest = aValidRegisterRequest();
        CoachCredentials credentials = Instancio.create(CoachCredentials.class);
        when(authWebMapper.toCredentials(registerRequest)).thenReturn(credentials);

        when(authService.register(credentials)).thenThrow(new EmailAlreadyExistsException(credentials.email()));

        mvc.perform(post(URL_PREFIX + "/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$[0].code").value("EMAIL_ALREADY_EXISTS"));

        verify(authWebMapper).toCredentials(registerRequest);
        verify(authService).register(credentials);
    }

    @Test
    void register_shouldReturnBadRequest_whenArgumentsNotValid() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("   ", "coach.com", "123");

        mvc.perform(post(URL_PREFIX + "/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void login_shouldReturnAuthResponse_whenAuthenticationIsValid() throws Exception {
        LoginRequest loginRequest = aValidLoginRequest();
        CoachAuthData authData = Instancio.create(CoachAuthData.class);
        when(authService.login(loginRequest.email(), loginRequest.rawPassword())).thenReturn(authData);
        AuthResponse authResponse = Instancio.create(AuthResponse.class);
        when(authWebMapper.toAuthResponse(authData)).thenReturn(authResponse);

        mvc.perform(post(URL_PREFIX + "/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value(authResponse.token()));

        verify(authService).login(loginRequest.email(), loginRequest.rawPassword());
        verify(authWebMapper).toAuthResponse(authData);
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreNotValid() throws Exception {
        LoginRequest loginRequest = aValidLoginRequest();
        when(authService.login(loginRequest.email(), loginRequest.rawPassword()))
            .thenThrow(new InvalidCredentialsException());

        mvc.perform(post(URL_PREFIX + "/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$[0].code").value("INVALID_CREDENTIALS"));

        verify(authService).login(loginRequest.email(), loginRequest.rawPassword());
    }

    @Test
    void login_shouldReturnBadRequest_whenArgumentsNotValid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("coach.com", "123");

        mvc.perform(post(URL_PREFIX + "/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    private RegisterRequest aValidRegisterRequest() {
        return Instancio.of(RegisterRequest.class)
            .generate(field(RegisterRequest::email), gen -> gen.net().email())
            .generate(field(RegisterRequest::rawPassword), gen -> gen.string().minLength(8))
            .create();
    }

    private LoginRequest aValidLoginRequest() {
        return Instancio.of(LoginRequest.class)
            .generate(field(LoginRequest::email), gen -> gen.net().email())
            .create();
    }

}
