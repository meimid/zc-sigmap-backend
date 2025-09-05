package com.sigmap.common.ref_params;


import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link RefParam}.
 *
 * This repository extends {@link JpaRepository}, {@link JpaSpecificationExecutor}, and
 * {@link SoftDeleteRepository} for performing common operations on RefParams.
 */
@Repository
public interface RefParamRepository  extends JpaRepository<RefParam, Integer>,
        JpaSpecificationExecutor<RefParam>,
        SoftDeleteRepository<RefParam, Integer> {

    @Query("SELECT r FROM RefParam r WHERE r.refType.refCode = :refType and r.id = :refPramId ")
    Optional <RefParam> findByIdAndRefType_Id(Integer refPramId, String refType) ;



    @Query("SELECT EXISTS (SELECT d FROM DynaDocs d WHERE d.categorieId.id = :refParamId)")
    boolean hasAssociatedDynaDoc(@Param("refParamId") Integer refParamId);

    Optional<RefParam> findByCode(String code);


}