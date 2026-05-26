package com.axel.trainingmetricsapi.controller.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAuthorizationHeaderIsAbsent() throws Exception {
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
        verify(jwtUtils, never()).extractCoachId(any());
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAuthorizationHeaderHasNoBearerPrefix() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
        verify(jwtUtils, never()).extractCoachId(any());
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenCoachIdIsNull() throws Exception {
        String token = "invalid.token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtils.extractCoachId(token)).thenReturn(null);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
        verify(jwtUtils).extractCoachId(token);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAuthenticationAlreadyExists() throws Exception {
        long coachId = 42L;
        String token = "valid.token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtils.extractCoachId(token)).thenReturn(coachId);
        UsernamePasswordAuthenticationToken existingAuth =
            new UsernamePasswordAuthenticationToken(coachId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
        assertThat(filterChain.getRequest()).isNotNull();
        verify(jwtUtils).extractCoachId(token);
    }

    @Test
    void doFilterInternal_shouldAuthenticate_whenTokenIsValid() throws Exception {
        long coachId = 42L;
        String token = "valid.token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtils.extractCoachId(token)).thenReturn(coachId);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(new AuthenticatedCoach(coachId));
        assertThat(authentication.getCredentials()).isNull();
        assertThat(authentication.getAuthorities()).isEmpty();
        assertThat(filterChain.getRequest()).isNotNull();
        verify(jwtUtils).extractCoachId(token);
    }
}
