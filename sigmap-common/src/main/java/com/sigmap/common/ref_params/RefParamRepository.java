package com.sigmap.common.ref_params;

import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("SELECT r FROM RefParam r WHERE r.refType.id = :refTypeId and r.id = :refPramId ")
   Optional <RefParam> findByIdAndRefType_Id(Integer refPramId, Integer refTypeId) ;



    @Query("SELECT EXISTS (SELECT d FROM DynaDocs d WHERE d.categorieId.id = :refParamId)")
    boolean hasAssociatedDynaDoc(@Param("refParamId") Integer refParamId);

    @Query("SELECT EXISTS (SELECT f FROM DynaFields f WHERE f.type_id.id = :refParamId)")
    boolean hasAssociatedDynaField(@Param("refParamId") Integer refParamId);

    @Query("SELECT EXISTS (SELECT fd FROM DynaFieldsDetails fd WHERE fd.typeId.id = :refParamId)")
    boolean hasAssociatedDynaFieldDetail(@Param("refParamId") Integer refParamId);

}
