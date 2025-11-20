package com.sigmap.common.societe;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SocieteRepository  extends JpaRepository<Societe, Integer>,
        JpaSpecificationExecutor<Societe>,
        SoftDeleteRepository<Societe, Integer> {

    @Query("SELECT EXISTS (SELECT cb FROM CompteBancaire cb WHERE cb.societe.id = :societeId)")
    boolean hasAssociatedCompteBancaire(@Param("societeId") Integer societeId);

}

