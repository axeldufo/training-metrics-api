package com.axel.trainingmetricsapi.interfaces.web;

import com.axel.trainingmetricsapi.interfaces.web.security.JwtUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

abstract class SecurityMockControllerSupport {

    @MockitoBean                 // Not used but Required by @WebMvcTest context
    protected JwtUtils jwtUtils; // JwtAuthenticationFilter depends on JwtUtils

}
