package com.axel.trainingmetricsapi.identity.interfaces.web.security;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class SecurityMockControllerSupport {

    @MockitoBean                 // Not used but Required by @WebMvcTest context
    protected JwtUtils jwtUtils; // JwtAuthenticationFilter depends on JwtUtils

}
