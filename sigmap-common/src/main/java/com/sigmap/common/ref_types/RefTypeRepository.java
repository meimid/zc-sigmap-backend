package com.sigmap.common.ref_types;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefTypeRepository extends JpaRepository<RefType, Integer>, JpaSpecificationExecutor<RefType>,
        SoftDeleteRepository<RefType, Integer> {
    @Query("SELECT EXISTS (SELECT rp FROM RefParam rp WHERE rp.refType.id = :refTypeId and rp.isDeleted=false)")
    boolean hasAssociatedRefParams(@Param("refTypeId") Integer refTypeId);
}
