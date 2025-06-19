package com.sigmap.common.realisations;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.repository.CompositeSoftDeleteRepository;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    @Query("SELECT r FROM Realisation r JOIN r.ppms ppm " +
            "WHERE ppm.id = :ppmId AND ppm.version = :ppmVer " +
            "AND NOT EXISTS (SELECT 1 FROM Dac d WHERE d.realisation.id = r.id AND d.realisation.version = r.version)")
    Page<Realisation> findRealisationsWithoutDacByPpm(@Param("ppmId") Integer ppmId,
                                                      @Param("ppmVer") Integer ppmVer,
                                                      Pageable pageable);
}

//public interface RealisationRepository extends JpaRepository<Realisation, RealisationId>,
//        JpaSpecificationExecutor<Realisation>,
//        SoftDeleteRepository<Realisation, Integer> {}