package com.sigmap.common.reunion_ppm_doc;

import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface ReunionPpmDocService {
    Optional<ReunionPpmDoc> findOne(Specification<ReunionPpmDoc> spec);
}
