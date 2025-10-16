package com.sigmap.common.dac_prix;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'accès aux données des prix DAC
 */
@Repository
public interface DacPrixRepository extends JpaRepository<DacPrix, DacPrixId>, JpaSpecificationExecutor<DacPrix> {

    @Query("SELECT dp FROM DacPrix dp  WHERE dp.id.dac.dacId =:dacId AND dp.id.dac.dacVer =:dacVer AND dp.id.monnaie.id =:monnaieId")
    Optional<DacPrix> findById( @Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer, @Param("monnaieId") Integer monnaieId);

    @Query("SELECT dp FROM DacPrix dp  WHERE dp.id.dac.dacId =:dacId AND dp.id.dac.dacVer =:dacVer ")
    List<DacPrix> findByDac (@Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer );


}
