package com.sigmap.common.model_ppm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ModelPpmRepository extends JpaRepository<ModelPpm, ModelPpmId> {
    Optional<ModelPpm> findByPpmIdAndPpmVerAndIsDeletedFalse(Integer ppmId, Integer ppmVer);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update ModelPpm m set m.isDeleted = true, m.deletedAt = current_timestamp, m.deletedBy = ?#{principal?.subject} where m.ppmId  = :ppmId and m.ppmVer = :ppmVer")
    void deleteByPpmIdAndPpmVer(Integer ppmId, Integer ppmVer);

    boolean existsByPpmIdAndPpmVer(Integer ppmId, Integer ppmVer);

    @Query("select d.alfresco_id from ModelPpm mp join mp.doc d where mp.ppmId = :ppmId and mp.ppmVer = :ppmVer and (d.isDeleted = false)")
    Optional<String> findModelAlfrescoId(@Param("ppmId") Integer ppmId,
                                         @Param("ppmVer") Integer ppmVer);
}

