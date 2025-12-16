package com.sigmap.common.realisations;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.StatusAppelOffre;
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
import java.util.Optional;

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

    @Query("SELECT r FROM Realisation r " +
            "WHERE EXISTS (SELECT 1 FROM Realisation r2 JOIN r2.ppms ppm " +
            "WHERE ppm.id = :ppmId AND ppm.version = :ppmVersion " +
            "AND r2.id = r.id AND r2.version = r.version) " +
            "AND r.version = (SELECT MAX(r3.version) FROM Realisation r3 " +
            "WHERE r3.id = r.id AND r3.isDeleted = false) " +
            "AND r.isDeleted = false")
    List<Realisation> findLatestRealisationVersionsByPpm(@Param("ppmId") Integer ppmId,
                                                         @Param("ppmVersion") Integer ppmVersion);

    /**
     * Finds the full Realisation entity with the highest version number for a given ID.
     * This method returns only non-deleted realisations.
     *
     * @param id The ID of the Realisation to find.
     * @return An Optional containing the Realisation entity with the latest version, or an empty Optional if not found.
     */
    @Query("SELECT r FROM Realisation r WHERE r.id = :id " +
            "AND r.isDeleted = false " +
            "AND r.version = (SELECT MAX(r2.version) FROM Realisation r2 WHERE r2.id = :id AND r2.isDeleted = false)")
    Optional<Realisation> findLatestVersionById(@Param("id") Integer id);

    @Query("SELECT r FROM Realisation r JOIN r.ppms ppm WHERE ppm.id = :ppmId AND ppm.version = :ppmVer " +
           " AND NOT EXISTS (SELECT 1 FROM AppelOffre ao WHERE ao.realisation.id = r.id AND ao.realisation.version = r.version " +
           "AND ao.statusAppelOffre IN :statuses)")
    List<Realisation> findRealisationsNotLinkedToAppelOffreWithStatus( @Param("ppmId") Integer ppmId,
                                                                       @Param("ppmVer") Integer ppmVer,
                                                                       @Param("statuses") List<StatusAppelOffre> statuses );
}

//public interface RealisationRepository extends JpaRepository<Realisation, RealisationId>,
//        JpaSpecificationExecutor<Realisation>,
//        SoftDeleteRepository<Realisation, Integer> {}