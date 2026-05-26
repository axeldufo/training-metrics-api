package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.controller.security.JwtUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

abstract class SecurityMockControllerSupport {

    @MockitoBean                 // Not used but Required by @WebMvcTest context
    protected JwtUtils jwtUtils; // JwtAuthenticationFilter depends on JwtUtils

}
