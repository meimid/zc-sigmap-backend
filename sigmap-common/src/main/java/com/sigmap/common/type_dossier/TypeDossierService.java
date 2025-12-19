package com.sigmap.common.type_dossier;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface TypeDossierService {
    List<TypeDossier> findAll(Specification<TypeDossier> spec);
    Optional<TypeDossier> findOne(Specification<TypeDossier> spec);
    TypeDossier findById(Integer id);
}
