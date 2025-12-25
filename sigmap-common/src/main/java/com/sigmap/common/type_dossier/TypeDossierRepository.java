package com.sigmap.common.type_dossier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeDossierRepository extends JpaRepository<TypeDossier, Integer>,
        JpaSpecificationExecutor<TypeDossier> {}
