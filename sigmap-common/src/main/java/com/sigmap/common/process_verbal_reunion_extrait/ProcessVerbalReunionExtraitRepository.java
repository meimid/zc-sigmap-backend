package com.sigmap.common.process_verbal_reunion_extrait;

import com.sigmap.common.entity.ExtraitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessVerbalReunionExtraitRepository
        extends JpaRepository<ProcessVerbalReunionExtrait, ExtraitId>,
        JpaSpecificationExecutor<ProcessVerbalReunionExtrait> {




    Page<ProcessVerbalReunionExtrait> findAllByPvIdAndIsDeletedFalse(Integer pvId, Pageable pageable);
    List<ProcessVerbalReunionExtrait> findAllByPvIdAndIsDeletedFalse(Integer pvId);
    Optional<ProcessVerbalReunionExtrait> findByExtraitIdAndIsDeletedFalse(Integer extraitId);
    @Query("""
   SELECT e
   FROM ProcessVerbalReunionExtrait e
   WHERE e.extraitId = :extraitId
     AND e.pvId = :pvId
     AND e.acId = :acId
     AND e.isDeleted = false

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
   AND e.isDeleted = false

""")
    Optional<ProcessVerbalReunionExtrait> findParAcByPvIdAndAcId(
            Integer pvId,
            Integer acId
    );



    @Query("""
SELECT e
FROM ProcessVerbalReunionExtrait e
WHERE e.doc IS NOT NULL
  AND e.ac.id IN :acIds
  AND e.isDeleted = false

  AND EXISTS (
        SELECT 1
        FROM Ppm p
        WHERE p.id = :ppmId
          AND p.version = :ppmVer
          AND p.ac.id = e.ac.id
          AND p.isDeleted = false
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
