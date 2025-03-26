package com.sigmap.common.agpm;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Agpm}.
 *
 * This repository extends {@link JpaRepository}, {@link JpaSpecificationExecutor}, and
 * {@link SoftDeleteRepository} for performing common operations on AGPM entities.
 */
@Repository
public interface AgpmRepository extends JpaRepository<Agpm, Integer>,
        JpaSpecificationExecutor<Agpm>,
        SoftDeleteRepository<Agpm, Integer> {
}
