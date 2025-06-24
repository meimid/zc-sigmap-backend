package com.sigmap.common.security;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

/**
 * Utility class related to authentication.
 */
public class Authentication {

    /**
     * Get the current user ID.
     *
     * @return the current user ID, or empty if not authenticated
     */
    public static Optional<String> getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(org.springframework.security.core.Authentication::isAuthenticated)
                .map(org.springframework.security.core.Authentication::getPrincipal)
                .map(Jwt.class::cast)
                .map(Jwt::getSubject);
    }
    public static boolean hasRole(String role) {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(auth -> auth.getAuthorities().stream()
                        .map(grantedAuthority -> ((GrantedAuthority) grantedAuthority).getAuthority().replace("ROLE_", ""))
                        .anyMatch(r -> r.equals(role))
                )
                .orElse(false);
    }

}
