package com.sigmap.common.lots;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.repository.CompositeSoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


public interface LotRepository extends JpaRepository<Lot, CompositeId>,JpaSpecificationExecutor<Lot>,
        CompositeSoftDeleteRepository<Lot, Integer> {

    @Query("SELECT COALESCE(MAX(l.id), 0) FROM Lot l")
    Integer findMaxId();
}
