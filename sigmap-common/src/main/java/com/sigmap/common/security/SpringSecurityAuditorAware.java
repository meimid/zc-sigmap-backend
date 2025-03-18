package com.sigmap.common.security;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Spring Security Auditor Aware.
 *
 * <p>This class is used by Spring Data JPA to determine the current auditor of
 * entities (to populate the createdBy, updatedBy fields).
 *
 * @see org.springframework.data.domain.AuditorAware
 * 
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    @SuppressWarnings("NullableProblems")
    @Override
    public Optional<String> getCurrentAuditor() {
        return Authentication.getCurrentUserId();
    }
}
