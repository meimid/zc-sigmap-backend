package com.sigmap.common.realisations;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.repository.CompositeSoftDeleteRepository;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RealisationRepository extends JpaRepository<Realisation, CompositeId>,
        JpaSpecificationExecutor<Realisation>,
        CompositeSoftDeleteRepository<Realisation, Integer> {

//    @Query("SELECT COALESCE(MAX(r.id), 0) FROM Realisation r")
//    Integer findMaxId();

    /**
     * Find the maximum version for a given realisation ID.
     *
     * @param id the realisation ID
     * @return the maximum version number for the specified ID, or null if not found
     */
    @Query("SELECT MAX(r.version) FROM Realisation r WHERE r.id = :id AND r.isDeleted = false")
    Integer findMaxVersionById(@Param("id") Integer id);
}

//public interface RealisationRepository extends JpaRepository<Realisation, RealisationId>,
//        JpaSpecificationExecutor<Realisation>,
//        SoftDeleteRepository<Realisation, Integer> {}