package com.sigmap.common.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Calendar;

@NoRepositoryBean
public interface CompositeSoftDeleteRepository<T, ID> {

    /**
     * Soft delete an entity by its composite ID components (id and version).
     *
     * <p>Requires entity to have two ID fields (e.g., lotsId/lotsVer), isDeleted, deletedAt, deletedBy properties.</p>
     * <p>Principle.subject requires SecurityEvaluationContextExtension bean to be defined.</p>
     *
     * @param id the first part of the composite ID (e.g., lotsId)
     * @param version the second part of the composite ID (e.g., lotsVer)
     * @return the number of entities updated (typically 1)
     */

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update #{#entityName} t set t.isDeleted = true, t.deletedAt = now(), t.deletedBy = :#{principal.subject} where t.id = ?1 and t.version = ?2")
    Integer softDelete(ID id, ID version);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update #{#entityName} t set t.isDeleted = true, t.deletedAt = :deletedAt, t.deletedBy = ?#{principal?.subject} where t.id = :id and t.version = :version")
    Integer softDelete(@Param("id") ID id,@Param("version") ID version, @Param("deletedAt") Calendar deletedAt);

}