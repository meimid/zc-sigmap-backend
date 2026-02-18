package com.sigmap.common.process_verbal_reunion_extrait_ppm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessVerbalReunionExtraitPpmRepository
        extends JpaRepository<ProcessVerbalReunionExtraitPpm, ProcessVerbalReunionExtraitPpmId>,
        JpaSpecificationExecutor<ProcessVerbalReunionExtraitPpm> {

    // find all PPM relations for one extrait
    List<ProcessVerbalReunionExtraitPpm> findByPvIdAndAcId(Integer pvId, Integer acId);

    Optional <ProcessVerbalReunionExtraitPpm> findByExtraitIdAndPvIdAndAcIdAndPpmIdAndPpmVer(
            Integer extraitId,
            Integer pvId,
            Integer acId,
            Integer ppmId,
            Integer ppmVer
    );
    @Query("""
 SELECT p FROM ProcessVerbalReunionExtraitPpm p
 WHERE p.pvId = :pvId
   AND p.acId = :acId
   AND p.ppmId = :ppmId
   AND p.ppmVer = :ppmVer
""")
    Optional<ProcessVerbalReunionExtraitPpm> findExistingPpmLink(
            Integer pvId,
            Integer acId,
            Integer ppmId,
            Integer ppmVer
    );


    @Query("""
SELECT p FROM ProcessVerbalReunionExtraitPpm p
WHERE p.acId = :acId
  AND p.ppmId = :ppmId
  AND p.ppmVer = :ppmVer
  AND p.extrait.doc IS NOT NULL
""")
    List<ProcessVerbalReunionExtraitPpm> findExistingExtraitByAcIdAndPpmIdAndPpmVer(
            Integer acId,
            Integer ppmId,
            Integer ppmVer
    );




}