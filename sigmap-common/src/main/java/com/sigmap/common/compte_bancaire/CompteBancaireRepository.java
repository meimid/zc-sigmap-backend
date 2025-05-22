package com.sigmap.common.compte_bancaire;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CompteBancaireRepository extends JpaRepository<CompteBancaire, Integer>,
        JpaSpecificationExecutor<CompteBancaire>,
        SoftDeleteRepository<CompteBancaire, Integer> {
    @Query("select c from CompteBancaire c where c.societe.id= :societeId and c.isDeleted=false")
    Page<CompteBancaire> findCompteBancaireBySocieteId(@Param("societeId") Integer societeId, Pageable pageable);


}

