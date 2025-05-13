package com.sigmap.common.lots;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.repository.CompositeSoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface LotRepository extends JpaRepository<Lot, CompositeId>,JpaSpecificationExecutor<Lot>,
        CompositeSoftDeleteRepository<Lot, Integer> {

    @Query("SELECT COALESCE(MAX(l.id), 0) FROM Lot l")
    Integer findMaxId();

    /**
     * Find the maximum version for a given lot ID.
     *
     * @param id the lot ID
     * @return the maximum version number for the specified ID, or null if not found
     */
    @Query("SELECT MAX(l.version) FROM Lot l WHERE l.id = :id AND l.isDeleted = false")
    Integer findMaxVersionById(@Param("id") Integer id);
}
