package com.sigmap.common.dac_prix;

import com.sigmap.common.repository.SoftDeleteRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'accès aux données des prix DAC
 */
@Repository
public interface DacPrixRepository extends JpaRepository<DacPrix, DacPrixId>, JpaSpecificationExecutor<DacPrix>
                                            , SoftDeleteRepository<DacPrix, DacPrixId> {

    @Query("SELECT dp FROM DacPrix dp  WHERE dp.id.dac.dacId =:dacId AND dp.id.dac.dacVer =:dacVer AND dp.id.monnaie.id =:monnaieId and dp.isDeleted = false")
    Optional<DacPrix> findById( @Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer, @Param("monnaieId") Integer monnaieId);

    @Query("SELECT dp FROM DacPrix dp  WHERE dp.id.dac.dacId =:dacId AND dp.id.dac.dacVer =:dacVer ")
    List<DacPrix> findByDac (@Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE DacPrix dp SET dp.isDeleted = true, dp.deletedAt = CURRENT_TIMESTAMP, dp.deletedBy = :#{principal.subject} " +
            "WHERE dp.id.dac.dacId = :dacId AND dp.id.dac.dacVer = :dacVer AND dp.id.monnaie.id = :monnaieId")
    void softDelete(@Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer, @Param("monnaieId") Integer monnaieId);
    
    default void softDelete(List<DacPrix> dacPrixList) {
        dacPrixList.forEach(dacPrix -> 
            softDelete(dacPrix.getId().getDac().getDacId(), 
                      dacPrix.getId().getDac().getDacVer(), 
                      dacPrix.getId().getMonnaie().getId())
        );
    }
}
