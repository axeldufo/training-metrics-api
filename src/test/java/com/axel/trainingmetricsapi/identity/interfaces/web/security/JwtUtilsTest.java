package com.axel.trainingmetricsapi.identity.interfaces.web.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        // JwtUtils uses @Value for secret and expiration
        // we don't want to start Spring context, so ReflectionTestUtils injects the values manually
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", "AX4KBDMfvrWxZwNGReaNnt+/zyB9QvO5idwBHFTQ8Yg=");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 86400000L);
        jwtUtils.init(); // must be called explicitly because @PostConstruct is not triggered outside Spring context
    }

    @Test
    void generateToken_shouldReturnWellFormedJwtToken() {
        String token = jwtUtils.generateToken(4L);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    void extractCoachId_shouldReturnCorrectCoachId () {
        long coachId = 4L;
        String token = jwtUtils.generateToken(coachId);

        assertThat(jwtUtils.extractCoachId(token)).isEqualTo(coachId);
    }

    @Test
    void extractCoachId_shouldReturnNull_whenTokenIsExpired () {
        ReflectionTestUtils.setField(jwtUtils, "expiration", -1000L);
        String token = jwtUtils.generateToken(4L);

        assertThat(jwtUtils.extractCoachId(token)).isNull();
    }

    @Test
    void validateToken_shouldReturnNull_whenTokenIsInvalid () {
        assertThat(jwtUtils.extractCoachId("invalid.token")).isNull();
    }


}
