package com.sigmap.common.process_verbal_reunion_extrait;

import com.sigmap.common.entity.ExtraitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessVerbalReunionExtraitRepository
        extends JpaRepository<ProcessVerbalReunionExtrait, ExtraitId> {



    Page<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId, Pageable pageable);
    List<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId);
    Optional<ProcessVerbalReunionExtrait> findByExtraitId(Integer extraitId);
    @Query("""
   SELECT e
   FROM ProcessVerbalReunionExtrait e
   WHERE e.extraitId = :extraitId
     AND e.pvId = :pvId
     AND e.acId = :acId
     AND e.extraitType = com.sigmap.common.entity.ExtraitType.PAR_AC
""")
    Optional<ProcessVerbalReunionExtrait> findByExtraitTypeAcByExtraitIdPvIdAcId(
            @Param("extraitId") Integer extraitId,
            @Param("pvId") Integer pvId,
            @Param("acId") Integer acId
    );

    @Query("""
 SELECT e FROM ProcessVerbalReunionExtrait e
 WHERE e.pvId = :pvId
   AND e.acId = :acId
   AND e.extraitType = com.sigmap.common.entity.ExtraitType.PAR_AC
""")
    Optional<ProcessVerbalReunionExtrait> findParAcByPvIdAndAcId(
            Integer pvId,
            Integer acId
    );


    @Query("""
    SELECT DISTINCT e
    FROM ProcessVerbalReunionExtrait e
    JOIN Reunion r ON r.processVerbal = e.processVerbal
    JOIN r.reunionPpms rp
    WHERE rp.ppmId = :ppmId
      AND rp.ppmVer = :ppmVer
      AND e.acId = :acId
      
""")
    List<ProcessVerbalReunionExtrait> findParDossierExtraitsByPpmAndAc(
            Integer ppmId,
            Integer ppmVer,
            Integer acId
    );


}
