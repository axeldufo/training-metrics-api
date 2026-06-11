package com.axel.trainingmetricsapi.application.port.out;

public interface PasswordEncoderPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}
