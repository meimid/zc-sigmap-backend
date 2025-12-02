package com.sigmap.common.process_verbal_reunion_extrait;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessVerbalReunionExtraitRepository
        extends JpaRepository<ProcessVerbalReunionExtrait, ExtraitId> {

    @Query("SELECT e FROM ProcessVerbalReunionExtrait e " +
            "WHERE e.extraitId = :extraitId " +
            "AND e.pvId = :pvId " +
            "AND e.acId = :acId")
    Optional<ProcessVerbalReunionExtrait> findByIds(
            @Param("extraitId") Integer extraitId,
            @Param("pvId") Integer pvId,
            @Param("acId") Integer acId
    );
}
