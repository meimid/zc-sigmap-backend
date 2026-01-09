package com.sigmap.common.source_financement;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link SourceFinancement}.
 *
 * This repository extends {@link JpaRepository},
 * {@link JpaSpecificationExecutor}, and
 * {@link SoftDeleteRepository} for performing common operations on
 * SourceFinancement.
 */
@Repository
public interface SourceFinancementRepository extends JpaRepository<SourceFinancement, Integer>,
        JpaSpecificationExecutor<SourceFinancement>,
        SoftDeleteRepository<SourceFinancement, Integer> {

}
