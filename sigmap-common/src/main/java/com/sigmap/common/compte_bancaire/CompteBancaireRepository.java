package com.sigmap.common.compte_bancaire;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CompteBancaireRepository extends JpaRepository<CompteBancaire, Integer>,
        JpaSpecificationExecutor<CompteBancaire>,
        SoftDeleteRepository<CompteBancaire, Integer> {}

