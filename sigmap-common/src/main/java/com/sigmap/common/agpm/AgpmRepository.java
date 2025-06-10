package com.sigmap.common.agpm;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.repository.CompositeSoftDeleteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Agpm entity.
 * Provides methods for database operations, including soft delete via SoftDeleteRepository.
 */
@Repository
public interface AgpmRepository extends JpaRepository<Agpm, CompositeId>, JpaSpecificationExecutor<Agpm>,
        CompositeSoftDeleteRepository<Agpm, Integer> {

//    @Query("SELECT MAX(p.id) FROM Agpm p")
//    Integer findMaxId();

    @Query("SELECT MAX(a.version) FROM Agpm a WHERE a.id = :id")
    Integer findMaxVersionById(@Param("id") Integer id);

    /**
     * Finds all Agpm entities by ID with pagination.
     *
     * @param agpmId   ID of the Agpm entity
     * @param pageable Pagination and sorting configuration
     * @return Page of Agpm entities for the given ID
     */
    @Query("SELECT a FROM Agpm a WHERE a.id = :agpmId")
    Page<Agpm> findAllById(Integer agpmId, Pageable pageable);

}