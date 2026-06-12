package com.axel.trainingmetricsapi.identity.application.port.out;

public interface PasswordEncoderPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}
