package com.sigmap.common.model_dac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ModelDacRepository extends JpaRepository<ModelDac, ModelDacId> {

    @Query("""
        select md
          from ModelDac md
         where md.dacId  = :dacId
           and md.dacVer = :dacVer
           and md.isAao  = :isAao
           and md.isDeleted = false
    """)
    Optional<ModelDac> findByDacIdAndDacVerAndIsAao(@Param("dacId") Integer dacId,
                                                          @Param("dacVer") Integer dacVer,
                                                          @Param("isAao") Boolean isAao);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update ModelDac m set m.isDeleted = true, m.deletedAt = current_timestamp, m.deletedBy = ?#{principal?.claims['sub']} where m.dacId  = :dacId and m.dacVer = :dacVer and m.isAao = :isAao")
    void deleteByDacIdAndDacVer(@Param("dacId") Integer dacId,
                                @Param("dacVer") Integer dacVer,
                                @Param("isAao") Boolean isAao);

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

    @Query("""
       SELECT COUNT(md) > 0
       FROM ModelDac md
       JOIN md.doc d
       JOIN md.dac dac
       WHERE d.alfresco_id = :alfrescoId
         AND dac.status = com.sigmap.common.entity.ProcessStatus.PUBLIE
         AND dac.shortListing = false
         AND dac.etapeDac = '1'
         AND d.isDeleted = false
         AND dac.isDeleted = false
       """)
    boolean isDocumentRelatedToDac(@Param("alfrescoId") String alfrescoId);

     @Query("""
        SELECT COUNT(md) > 0
        FROM ModelDac md
        JOIN md.doc d
        JOIN md.dac dac
        WHERE d.alfresco_id = :alfrescoId
          AND dac.status = com.sigmap.common.entity.ProcessStatus.PUBLIE
          AND (dac.shortListing = true OR dac.etapeDac = '2')
          AND d.isDeleted = false
          AND dac.isDeleted = false
          AND EXISTS (
              SELECT sd FROM SocieteDac sd
              WHERE sd.id.dac.dacId = dac.dacId
                AND sd.id.dac.dacVer = dac.dacVer
                AND sd.id.societe.id IN (
                    SELECT us.societeId FROM UserSociete us
                    WHERE us.userId = :userId AND us.isDeleted = false
                )
          )
        """)
     boolean isDocumentRelatedToDacShortlistForUser(@Param("alfrescoId") String alfrescoId, @Param("userId") String userId);
}
