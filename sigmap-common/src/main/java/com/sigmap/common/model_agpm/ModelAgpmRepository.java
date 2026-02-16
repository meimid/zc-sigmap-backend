package com.sigmap.common.model_agpm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ModelAgpmRepository extends JpaRepository<ModelAgpm, ModelAgpmId>, JpaSpecificationExecutor<ModelAgpm> {
    Optional<ModelAgpm> findByAgpmIdAndAgpmVer(Integer agpmId, Integer agpmVer);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update ModelAgpm m set m.isDeleted = true, m.deletedAt = current_timestamp, m.deletedBy = ?#{principal?.name}where m.agpmId  = :agpmId and m.agpmVer = :agpmVer")
    void deleteByAgpmIdAndAgpmVer(Integer agpmId, Integer agpmVer);

    boolean existsByAgpmIdAndAgpmVer(Integer agpmId, Integer agpmVer);

    @Query("select d.alfresco_id from ModelAgpm mp join mp.doc d where mp.agpmId = :agpmId and mp.agpmVer = :agpmVer and (d.isDeleted = false)")
    Optional<String> findModelAlfrescoId(@Param("agpmId") Integer agpmId,
                                         @Param("agpmVer") Integer agpmVer);
}

