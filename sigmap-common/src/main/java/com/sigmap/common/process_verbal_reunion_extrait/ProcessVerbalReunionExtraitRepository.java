package com.sigmap.common.process_verbal_reunion_extrait;

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

    @Query("SELECT e FROM ProcessVerbalReunionExtrait e " +
            "WHERE  e.pvId = :pvId " +
            "AND e.acId = :acId " +
            "AND e.ppm.id = :ppmId " +
            "AND e.ppm.version = :ppmVer" )
    Optional<ProcessVerbalReunionExtrait> findByIds(
            @Param("pvId") Integer pvId,
            @Param("acId") Integer acId,
            @Param("ppmId") Integer ppmId,
            @Param("ppmVer") Integer ppmVer
    );

    Page<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId, Pageable pageable);
    List<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId);

    @Query("SELECT e FROM ProcessVerbalReunionExtrait e " +
            "WHERE  e.ppm.id = :ppmId " +
            "AND e.ppm.version = :ppmVersion " +
            "AND e.acId = :acId " )
    List<ProcessVerbalReunionExtrait> findAllByAcIdAndPpmIdAndPpmVersion(@Param("acId")Integer acId, @Param("ppmId") Integer ppmId,@Param("ppmVersion") Integer ppmVersion);
}
