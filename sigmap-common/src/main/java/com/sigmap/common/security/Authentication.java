package com.sigmap.common.security;


import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public class Authentication {

    public static Optional<String> getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(org.springframework.security.core.Authentication::isAuthenticated)
                .map(org.springframework.security.core.Authentication::getPrincipal)
                .map(Jwt.class::cast)
                .map(Jwt::getSubject);
    }
}
