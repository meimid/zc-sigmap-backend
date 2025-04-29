package com.sigmap.common.agpm;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.repository.CompositeSoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Agpm entity.
 * Provides methods for database operations, including soft delete via SoftDeleteRepository.
 */
@Repository
public interface AgpmRepository extends JpaRepository<Agpm, CompositeId>, JpaSpecificationExecutor<Agpm>,
        CompositeSoftDeleteRepository<Agpm, Integer> {

    @Query("SELECT MAX(p.id) FROM Agpm p")
    Integer findMaxId();
}