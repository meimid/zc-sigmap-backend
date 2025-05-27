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

    @Query("SELECT EXISTS (SELECT c FROM Com c WHERE c.comType.id = :refParamId and c.isDeleted=false)")
    boolean hasAssociatedCom(@Param("refParamId") Integer refParamId);

    @Query("SELECT EXISTS (SELECT cb FROM CompteBancaire cb WHERE cb.pays.id = :refParamId and cb.isDeleted=false)")
    boolean hasAssociatedCompteBancaire(@Param("refParamId") Integer refParamId);

    @Query("SELECT EXISTS (SELECT s FROM Societe s WHERE s.pays.id = :refParamId or s.type.id = :refParamId and s.isDeleted=false)")
    boolean hasAssociatedSociete(@Param("refParamId") Integer refParamId);

    @Query("SELECT EXISTS (SELECT sa FROM SocieteAddress sa WHERE sa.type.id = :refParamId and sa.isDeleted=false)")
    boolean hasAssociatedSocieteAddress(@Param("refParamId") Integer refParamId);

    @Query("SELECT r FROM RefParam r WHERE r.refType.id = :refTypeId and r.isDeleted=false and r.refType.isDeleted=false")
    List<RefParam> findByRefType(@Param("refTypeId") Integer refTypeId);

   Optional <RefParam> findByIdAndRefType_Id(Integer refPramId, Integer refTypeId) ;

    @Query("SELECT EXISTS (SELECT p FROM Ppm p WHERE p.status.id = :refParamId and p.isDeleted=false and p.status.isDeleted=false)")
    boolean hasAssociatedPpm(@Param("refParamId") Integer refParamId);
    @Query("SELECT EXISTS (SELECT p FROM Paa p WHERE p.status.id = :refParamId and p.isDeleted=false and p.status.isDeleted=false)")
    boolean hasAssociatedPaa(@Param("refParamId") Integer refParamId);
    @Query("SELECT EXISTS (SELECT a FROM Agpm a WHERE a.status.id = :refParamId and a.isDeleted=false and a.status.isDeleted=false)")
    boolean hasAssociatedAgpm(@Param("refParamId") Integer refParamId);
    @Query("SELECT CASE WHEN EXISTS (" +
            "SELECT 1 FROM Realisation r " +
            "LEFT JOIN r.service s " +
            "LEFT JOIN r.direction d " +
            "LEFT JOIN r.sourceFinancement sf " +
            "LEFT JOIN r.monais m " +
            "LEFT JOIN r.typeMarcher tm " +
            "LEFT JOIN r.modeSelection ms " +
            "WHERE ((s.id = :refParamId AND s.isDeleted = false) OR " +
            "(d.id = :refParamId AND d.isDeleted = false) OR " +
            "(sf.id = :refParamId AND sf.isDeleted = false) OR " +
            "(m.id = :refParamId AND m.isDeleted = false) OR " +
            "(tm.id = :refParamId AND tm.isDeleted = false) OR " +
            "(ms.id = :refParamId AND ms.isDeleted = false)) " +
            "AND r.isDeleted = false) THEN true ELSE false END")
    boolean hasAssociatedRealisation(@Param("refParamId") Integer refParamId);



}
