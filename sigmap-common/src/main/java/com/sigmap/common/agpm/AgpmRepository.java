package com.sigmap.common.agpm;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.repository.CompositeSoftDeleteRepository;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository interface for Agpm entity.
 * Provides methods for database operations, including soft delete via SoftDeleteRepository.
 */
public interface AgpmRepository extends JpaRepository<Agpm, CompositeId>, JpaSpecificationExecutor<Agpm>,
        CompositeSoftDeleteRepository<Agpm, Integer> {

}