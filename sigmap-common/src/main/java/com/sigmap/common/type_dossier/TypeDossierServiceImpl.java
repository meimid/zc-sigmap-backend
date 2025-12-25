package com.sigmap.common.type_dossier;

import com.sigmap.common.type_dossier.exception.TypeDossierNotFound;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class TypeDossierServiceImpl implements TypeDossierService {
    private final TypeDossierRepository repository;

    public TypeDossierServiceImpl(TypeDossierRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TypeDossier> findAll(Specification<TypeDossier> spec) {
        return repository.findAll(spec);
    }

    @Override
    public Optional<TypeDossier> findOne(Specification<TypeDossier> spec) {
        return repository.findOne(spec);
    }

    @Override
    public TypeDossier findById(Integer id) {
        Specification<TypeDossier> spec= TypeDossierSpecifications.id(id);
        return findOne(spec).orElseThrow(()-> new TypeDossierNotFound(""));
    }
}
