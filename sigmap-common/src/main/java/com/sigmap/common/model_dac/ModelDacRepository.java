package com.sigmap.common.model_dac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ModelDacRepository extends JpaRepository<ModelDac, ModelDacId> {

    Optional<ModelDac> findByDacIdAndDacVer(Integer dacId, Integer dacVer);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update ModelDac m set m.isDeleted = true, m.deletedAt = current_timestamp, m.deletedBy = ?#{principal?.claims['sub']} where m.dacId  = :dacId and m.dacVer = :dacVer")
    void deleteByDacIdAndDacVer(@Param("dacId") Integer dacId,
                                @Param("dacVer") Integer dacVer);

    boolean existsByDacIdAndDacVer(Integer dacId, Integer dacVer);

    @Query("""
           select d.alfresco_id
             from ModelDac md
             join md.doc d
            where md.dacId  = :dacId
              and md.dacVer = :dacVer
              and (d.isDeleted = false)
           """)
    Optional<String> findModelAlfrescoId(@Param("dacId") Integer dacId,
                                         @Param("dacVer") Integer dacVer);
}
