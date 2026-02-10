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
""")
    Optional<ProcessVerbalReunionExtrait> findByExtraitIdPvIdAcId(
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


/*    @Query("""
SELECT DISTINCT e
FROM ProcessVerbalReunionExtrait e
JOIN Ppm p ON p.id = :ppmId AND p.version = :ppmVer
LEFT JOIN e.extraitsPpm ep
LEFT JOIN Reunion r ON r.processVerbal = e.processVerbal
LEFT JOIN r.reunionPpms rp
WHERE e.doc IS NOT NULL
AND e.ac.id IN :acIds
AND e.ac.id = p.ac.id
AND (
      (e.extraitType = com.sigmap.common.entity.ExtraitType.PAR_DOSSIER
       AND ep.ppmId = :ppmId
       AND ep.ppmVer = :ppmVer)
   OR (e.extraitType = com.sigmap.common.entity.ExtraitType.PAR_AC
       AND rp.ppmId = :ppmId
       AND rp.ppmVer = :ppmVer)
)
ORDER BY e.extraitId
""")
    List<ProcessVerbalReunionExtrait> findExtraitsByPpmAndAcs(
            @Param("acIds") List<Integer> acIds,
            @Param("ppmId") Integer ppmId,
            @Param("ppmVer") Integer ppmVer
    );*/




    @Query("""
SELECT e
FROM ProcessVerbalReunionExtrait e
WHERE e.doc IS NOT NULL
  AND e.ac.id IN :acIds

  AND EXISTS (
        SELECT 1
        FROM Ppm p
        WHERE p.id = :ppmId
          AND p.version = :ppmVer
          AND p.ac.id = e.ac.id
  )
          AND EXISTS (
                SELECT 1
                FROM Reunion r
                JOIN r.reunionPpms rp
                WHERE r.processVerbal = e.processVerbal
                  AND rp.ppmId = :ppmId
                  AND rp.ppmVer = :ppmVer
          
        
  )
ORDER BY e.extraitId
""")
    List<ProcessVerbalReunionExtrait> findExtraitsByPpmAndAcs(
            @Param("acIds") List<Integer> acIds,
            @Param("ppmId") Integer ppmId,
            @Param("ppmVer") Integer ppmVer
    );


}
