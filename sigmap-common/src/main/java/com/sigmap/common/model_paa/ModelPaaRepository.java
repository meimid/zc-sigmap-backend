package com.sigmap.common.model_paa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ModelPaaRepository extends JpaRepository<ModelPaa, ModelPaaId>, JpaSpecificationExecutor<ModelPaa> {
    Optional<ModelPaa> findByPaaIdAndPaaVer(Integer paaId, Integer paaVer);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update ModelPaa m set m.isDeleted = true, m.deletedAt = current_timestamp, m.deletedBy = ?#{principal?.name}where m.paaId  = :paaId and m.paaVer = :paaVer")
    void deleteByPaaIdAndPaaVer(Integer paaId, Integer paaVer);

    boolean existsByPaaIdAndPaaVer(Integer paaId, Integer paaVer);

    @Query("select d.alfresco_id from ModelPaa mp join mp.doc d where mp.paaId = :paaId and mp.paaVer = :paaVer and (d.isDeleted = false)")
    Optional<String> findModelAlfrescoId(@Param("paaId") Integer paaId,
                                         @Param("paaVer") Integer paaVer);
}

