package com.sigmap.common.ppms;

import com.sigmap.common.entity.CompositeId;

import com.sigmap.common.repository.CompositeSoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PpmRepository extends JpaRepository<Ppm, CompositeId>,
        JpaSpecificationExecutor<Ppm>,
        CompositeSoftDeleteRepository<Ppm, Integer> {
//    @Query("SELECT COALESCE(MAX(p.id), 0) FROM Ppm p")
//    Integer findMaxId();


    /**
     * Find the maximum version for a given PPM ID.
     *
     * @param id the PPM ID
     * @return the maximum version number for the specified ID, or null if not found
     */
    @Query("SELECT MAX(p.version) FROM Ppm p WHERE p.id = :id AND p.isDeleted = false")
    Integer findMaxVersionById(@Param("id") Integer id);

    /**
     * Finds all Ppm entities by ID with pagination.
     *
     * @param ppmId   ID of the Ppm entity
     * @param pageable Pagination and sorting configuration
     * @return Page of Ppm entities for the given ID
     */
    @Query("SELECT p FROM Ppm p WHERE p.id = :ppmId")
    Page<Ppm> findAllById(Integer ppmId, Pageable pageable);
}

