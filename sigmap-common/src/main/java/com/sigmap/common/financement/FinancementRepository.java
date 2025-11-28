package com.sigmap.common.financement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancementRepository extends JpaRepository<Financement, FinancementId>,
        JpaSpecificationExecutor<Financement> {

    /**
     * Find all financements for a specific realisation.
     * FIXED: use f.id.realisationId AND f.id.realisationVer
     */
    @Query("""
        SELECT f 
        FROM Financement f 
        WHERE f.id.realisationId = :realisationId 
          AND f.id.realisationVer = :realisationVer
        """)
    List<Financement> findByRealisationIdAndVersion(
            @Param("realisationId") Integer realisationId,
            @Param("realisationVer") Integer realisationVer);


    /**
     * Find financement by specific realisation and monnaie.
     * FIXED: use f.id.realisationId AND f.id.realisationVer
     */
    @Query("""
        SELECT f 
        FROM Financement f 
        WHERE f.id.realisationId = :realisationId 
          AND f.id.realisationVer = :realisationVer 
          AND f.monnaie.id = :monnaieId
        """)
    Financement findByRealisationAndMonnaie(
            @Param("realisationId") Integer realisationId,
            @Param("realisationVer") Integer realisationVer,
            @Param("monnaieId") Integer monnaieId);


    /**
     * Find financement by realisation composite PK and financementId.
     * FIXED: f.id.realisationId / f.id.realisationVer are correctly referenced
     */
    @Query("""
        SELECT f 
        FROM Financement f 
        WHERE f.id.realisationId = :realisationId 
          AND f.id.realisationVer = :realisationVersion 
          AND f.id.financementId = :financementId
        """)
    Optional<Financement> findByRealisationIdAndVersionAndFinancementId(
            @Param("realisationId") Integer realisationId,
            @Param("realisationVersion") Integer realisationVersion,
            @Param("financementId") Integer financementId);
}
