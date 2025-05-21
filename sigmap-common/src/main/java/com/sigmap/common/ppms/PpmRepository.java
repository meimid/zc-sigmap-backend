package com.sigmap.common.ppms;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.repository.CompositeSoftDeleteRepository;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PpmRepository extends JpaRepository<Ppm, CompositeId>,
        JpaSpecificationExecutor<Ppm>,
        CompositeSoftDeleteRepository<Ppm, Integer> {
    @Query("SELECT COALESCE(MAX(p.id), 0) FROM Ppm p")
    Integer findMaxId();


    /**
     * Find the maximum version for a given PPM ID.
     *
     * @param id the PPM ID
     * @return the maximum version number for the specified ID, or null if not found
     */
    @Query("SELECT MAX(p.version) FROM Ppm p WHERE p.id = :id AND p.isDeleted = false")
    Integer findMaxVersionById(@Param("id") Integer id);
}

