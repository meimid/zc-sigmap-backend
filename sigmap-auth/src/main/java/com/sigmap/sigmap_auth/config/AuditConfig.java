package com.sigmap.sigmap_auth.config;

import java.security.Principal;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class AuditConfig {

  @Bean
  public AuditorAware<String> auditorProvider() {
    return () ->
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(Principal::getName);
  }
}

