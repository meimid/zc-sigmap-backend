package com.sigmap.common.process_verbal_reunion_extrait_dac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessVerbalReunionExtraitDacRepository extends JpaRepository
        <ProcessVerbalReunionExtraitDac, ProcessVerbalReunionExtraitDacId> {

    // find all DAC relations for one extrait
    List<ProcessVerbalReunionExtraitDac> findByPvIdAndAcId(Integer pvId, Integer acId);

    @Query("""
 SELECT d FROM ProcessVerbalReunionExtraitDac d
 WHERE d.pvId = :pvId
   AND d.acId = :acId
   AND d.dacId = :dacId
   AND d.dacVer = :dacVer
""")
    Optional<ProcessVerbalReunionExtraitDac> findExistingDacLink(
            Integer pvId,
            Integer acId,
            Integer dacId,
            Integer dacVer
    );

}