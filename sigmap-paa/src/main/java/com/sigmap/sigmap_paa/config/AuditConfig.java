package com.sigmap.sigmap_paa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Optional;

@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
<<<<<<< Updated upstream
        return () -> {
            Optional<String> auditor = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                    .map(authentication -> authentication.getName());
            return auditor.isPresent() ? auditor : Optional.of(null);
        };
=======
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName);
>>>>>>> Stashed changes
    }
}