package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.controller.security.JwtUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WithMockUser // Needed on all privates endpoints tested to mock user
abstract class BaseControllerTest {

    @MockitoBean                 // Not used but Required by @WebMvcTest context
    protected JwtUtils jwtUtils; // JwtAuthenticationFilter depends on JwtUtils

}
