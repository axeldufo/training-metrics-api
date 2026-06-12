package com.axel.trainingmetricsapi.interfaces.web.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the authenticated coach from the security context.
 *
 * This component exists to keep controllers testable with @WebMvcTest.
 * Using @AuthenticationPrincipal directly on controller method parameters
 * causes conflicts with Spring Data's ProxyingHandlerMethodArgumentResolver
 * when a custom record (not UserDetails) is used as principal — the resolver
 * intercepts the argument before AuthenticationPrincipalArgumentResolver can.
 *
 * See: <a href="https://github.com/spring-projects/spring-data-commons/issues/2937">...</a>
 */
@Component
public class AuthenticatedCoachResolver {
    public AuthenticatedCoach resolve() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authentication found in security context");
        }
        return (AuthenticatedCoach) authentication.getPrincipal();
    }
}
