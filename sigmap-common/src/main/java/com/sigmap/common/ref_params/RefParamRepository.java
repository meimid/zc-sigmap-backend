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

    @Query("SELECT rp FROM RefParam rp WHERE rp.description = :description AND rp.refType.description = :refTypeDescription")
    Optional<RefParam> findByDescriptionAndRefTypeDescription(
            @Param("description") String description,
            @Param("refTypeDescription") String refTypeDescription
    );

    @Query("SELECT EXISTS (SELECT c FROM Com c WHERE c.comType.id = :refParamId and c.isDeleted=false )")
    boolean hasAssociatedCom(@Param("refParamId") Integer refParamId);

    @Query("SELECT EXISTS (SELECT cb FROM CompteBancaire cb WHERE cb.pays.id = :refParamId and cb.isDeleted=false )")
    boolean hasAssociatedCompteBancaire(@Param("refParamId") Integer refParamId);

    @Query("SELECT EXISTS (SELECT s FROM Societe s WHERE s.pays.id = :refParamId or s.type.id = :refParamId and s.isDeleted=false )")
    boolean hasAssociatedSociete(@Param("refParamId") Integer refParamId);

    @Query("SELECT EXISTS (SELECT sa FROM SocieteAddress sa WHERE sa.type.id = :refParamId and sa.isDeleted=false )")
    boolean hasAssociatedSocieteAddress(@Param("refParamId") Integer refParamId);

    @Query("SELECT r FROM RefParam r WHERE r.refType.id = :refTypeId and r.isDeleted=false")
    List<RefParam> findByRefType(@Param("refTypeId") Integer refTypeId);

   Optional <RefParam> findByIdAndRefType_Id(Integer refPramId, Integer refTypeId) ;

}
