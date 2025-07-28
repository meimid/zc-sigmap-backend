package com.sigmap.common.docs;

import com.sigmap.common.repository.SoftDeleteRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Docs entity.
 * Provides methods to interact with the docs table.
 */
public interface DocsRepository extends JpaRepository<Docs, Integer> , JpaSpecificationExecutor<Docs>,
        SoftDeleteRepository<Docs, Integer> {
    /**
     * Finds Docs entities by a list of IDs.
     *
     * @param ids List of doc IDs to find
     * @return List of Docs entities
     */
    List<Docs> findByIdIn(List<Integer> ids);

    @Query("SELECT d FROM Docs d WHERE d.alfresco_id = :alfresco_id and d.isDeleted=false")
    Optional<Docs> findDocsByAlfrescoId(@Param("alfresco_id") String alfrescoId);

    @Query("SELECT d FROM Docs d JOIN d.paas p WHERE p.id = :paaId AND p.version = :paaVersion and d.isDeleted=false and p.isDeleted=false")
    List<Docs> findDocsByPaasIdAndPaasVersion(@Param("paaId") Integer paaId, @Param("paaVersion") Integer paaVersion);

    @Query("SELECT d FROM Docs d JOIN d.realisations r WHERE r.id = :realisationId AND r.version = :realisationVersion and d.isDeleted=false and r.isDeleted=false")
    List<Docs> findDocsByRealisationIdAndRealisationVersion(@Param("realisationId") Integer realisationId, @Param("realisationVersion") Integer realisationVersion);
    @Query("SELECT d FROM Docs d JOIN d.ppms p WHERE p.id = :ppmId AND p.version = :ppmVersion and d.isDeleted=false and p.isDeleted=false")
    List<Docs> findDocsByPpmIdAndPpmVersion(@Param("ppmId") Integer ppmId, @Param("ppmVersion") Integer ppmVersion);

    @Query("SELECT d.name FROM Docs d WHERE d.isDeleted = false")
    List<String> findAllDocumentNames();
    @Modifying
    @Transactional
    @Query("UPDATE Docs d SET d.isDeleted = true, d.deletedAt = CURRENT_TIMESTAMP, d.deletedBy = :#{principal.subject} WHERE d.id IN :docIds")
    void softDeleteAllById(@Param("docIds") List<Integer> docIds);
    @Modifying
    @Transactional
    @Query("UPDATE Docs d SET d.isDeleted = true, d.deletedAt = CURRENT_TIMESTAMP, d.deletedBy = :#{principal.subject} " +
            "WHERE d.id IN (SELECT pd.docId FROM PvDocs pd WHERE pd.ppmId = :ppmId AND pd.ppmVersion = :ppmVersion AND pd.processVerbalId = :processVerbalId)")
    void softDeleteByPpmIdAndPpmVersionAndProcessVerbalId(
            @Param("ppmId") Integer ppmId,
            @Param("ppmVersion") Integer ppmVersion,
            @Param("processVerbalId") String processVerbalId);

    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("update #{#entityName} t set t.isDeleted = true, t.deletedAt = now(), t.deletedBy = ?#{principal?.subject} where t.id = ?1")
    Integer softDeleteDoc(Integer id);

}