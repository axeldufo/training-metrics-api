package com.axel.trainingmetricsapi.identity.infrastructure.security;

import com.axel.trainingmetricsapi.identity.infrastructure.security.BcryptPasswordEncoderAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BcryptPasswordEncoderAdapterTest {

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private BcryptPasswordEncoderAdapter adapter;

    @Test
    void encode_shouldDelegateToBCrypt() {
        when(passwordEncoder.encode("raw")).thenReturn("hashed");
        assertThat(adapter.encode("raw")).isEqualTo("hashed");
    }

    @Test
    void matches_shouldDelegateToBCrypt() {
        when(passwordEncoder.matches("raw", "hashed")).thenReturn(true);
        assertThat(adapter.matches("raw", "hashed")).isTrue();
    }
}
