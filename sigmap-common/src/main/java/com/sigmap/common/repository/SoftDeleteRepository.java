package com.sigmap.common.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface SoftDeleteRepository<T, ID>  {

    /**
     * softDelete.
     *
     * <p>Requires entity have id,isDeleted,deletedAt,deletedBy properties</p>
     * <p>Principle.subject requires SecurityEvaluationContextExtension bean to be defined </p>
     *
     * @param id ID
     * @return int
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update #{#entityName} t set t.isDeleted = true, t.deletedAt = now(), t.deletedBy = ?#{principal?.subject} where id = ?1")
    Integer softDelete(ID id);
}
