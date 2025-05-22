package com.sigmap.common.ac;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AcRepository extends JpaRepository<Ac, Integer>,
        JpaSpecificationExecutor<Ac>,
        SoftDeleteRepository<Ac, Integer> {
    @Query("SELECT EXISTS (SELECT com FROM Com com JOIN com.acs ac WHERE ac.id = :acId and com.isDeleted=false and ac.isDeleted=false)")
    boolean hasAssociatedCom(@Param("acId") Integer acId);
}

