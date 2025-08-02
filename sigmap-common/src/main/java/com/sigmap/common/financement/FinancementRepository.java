package com.sigmap.common.financement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancementRepository extends JpaRepository<Financement, FinancementId>, JpaSpecificationExecutor<Financement> {

    /**
     * Find all financements for a specific realisation.
     */
    @Query("SELECT f FROM Financement f WHERE f.id.realisation.id = :realisationId AND f.id.realisation.version = :realisationVer")
    List<Financement> findByRealisationIdAndVersion(@Param("realisationId") Integer realisationId,
                                                    @Param("realisationVer") Integer realisationVer);

    /**
     * Find financement by specific realisation and monnaie.
     */
    @Query("SELECT f FROM Financement f WHERE f.id.realisation.id = :realisationId AND f.id.realisation.version = :realisationVer AND f.id.monnaie.id = :monnaieId")
    Financement findByRealisationAndMonnaie(@Param("realisationId") Integer realisationId,
                                            @Param("realisationVer") Integer realisationVer,
                                            @Param("monnaieId") Integer monnaieId);
}
