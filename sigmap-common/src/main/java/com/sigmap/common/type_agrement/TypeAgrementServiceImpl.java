package com.sigmap.common.type_agrement;

import com.sigmap.common.activite.exception.ActiviteAlreadyExist;
import com.sigmap.common.activite.exception.RequiredLibelle;
import com.sigmap.common.config.RefTypes;
import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeService;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import com.sigmap.common.type_agrement.exception.OrganismeDeDeliveranceAgrementsRequired;
import com.sigmap.common.type_agrement.exception.TypeAgrementAlreadyExist;
import com.sigmap.common.type_agrement.exception.TypeAgrementNotFound;
import com.sigmap.common.type_agrement.exception.WrongType;
import com.sigmap.common.type_agrement.mapper.TypeAgrementMapper;
import com.sigmap.common.type_agrement.request.TypeAgrementRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
@Service
public class TypeAgrementServiceImpl implements TypeAgrementService {
    private final TypeAgrementRepository repository;
    private final RefTypeService refTypeService;

    public TypeAgrementServiceImpl(TypeAgrementRepository repository, RefTypeService refTypeService) {
        this.repository = repository;
        this.refTypeService = refTypeService;
    }

    @Override
    public List<TypeAgrement> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<TypeAgrement> findAll(Specification<TypeAgrement> typeAgrement, Pageable pageable) {
        typeAgrement=typeAgrement.and(TypeAgrementSpecifications.deleted(false));
        return repository.findAll(typeAgrement, pageable);
    }

    @Override
    public Page<TypeAgrement> findAllDeleted(Specification<TypeAgrement> typeAgrement, Pageable pageable) {
        typeAgrement=TypeAgrementSpecifications.deleted(true);
        return repository.findAll(typeAgrement, pageable);
    }

    @Override
    public Page<TypeAgrement> findAllDeleted(Pageable pageable) {
        Specification<TypeAgrement> TypeAgrement=TypeAgrementSpecifications.deleted(true);
        return repository.findAll(TypeAgrement, pageable);
    }

    @Override
    public Optional<TypeAgrement> findDeletedById(Integer id) {
        Specification<TypeAgrement> TypeAgrement=TypeAgrementSpecifications.deleted(true).and(TypeAgrementSpecifications.id(id));
        return repository.findOne(TypeAgrement);
    }

    @Override
    public Optional<TypeAgrement> findOne(Specification<TypeAgrement> specification) {
        return repository.findOne(specification);
    }

    @Override
    public TypeAgrement findById(Integer id) throws TypeAgrementNotFound {
        Specification<TypeAgrement> TypeAgrement=TypeAgrementSpecifications.deleted(false).and(TypeAgrementSpecifications.id(id));
        return repository.findOne(TypeAgrement).orElseThrow(()->new TypeAgrementNotFound("",id));    }

    @Override
    public TypeAgrement save(TypeAgrement typeAgrement) {
        return repository.save(typeAgrement);     }

    @Override
    public TypeAgrement create(TypeAgrementRequest request) throws RefTypeNotFound {
        if(request.getLibelle()==null||request.getLibelle().isEmpty()) {
            throw new RequiredLibelle("");
        }
        Specification<TypeAgrement> specification = TypeAgrementSpecifications.deleted(false).and(TypeAgrementSpecifications.libelle(request.getLibelle()));

        if(findOne(specification).isPresent()) {
            throw new TypeAgrementAlreadyExist("", request.getLibelle());
        }
        if(request.getOrganismeDeDeliveranceAgrementsId()==null) {
            throw new OrganismeDeDeliveranceAgrementsRequired("Organisme de Deliverance Agrements is required ");
        }
        RefType organismeDeDeliveranceAgrements=refTypeService.findById(request.getOrganismeDeDeliveranceAgrementsId());
        if (!organismeDeDeliveranceAgrements.getRefCode().equals(RefTypes.ORGANISME_DE_DELIVRANCE_AGREMENTS.name())) {
            throw new WrongType("");
        }
        TypeAgrement typeAgrement= TypeAgrementMapper.INSTANCE.mapFromRequest(request);
        typeAgrement.setOrganismeDeDeliveranceAgrements(organismeDeDeliveranceAgrements);
        return repository.save(typeAgrement);
    }

    @Override
    public TypeAgrement update(Integer id, TypeAgrementRequest request) throws TypeAgrementNotFound, RefTypeNotFound {
        TypeAgrement typeAgrement=findById(id);

        if(request.getOrganismeDeDeliveranceAgrementsId()==null) {
            throw new OrganismeDeDeliveranceAgrementsRequired("Organisme de Deliverance Agrements is required ");
        }
        if(request.getLibelle()==null||request.getLibelle().isEmpty()) {
            throw new RequiredLibelle("");
        }
        else {
            Specification<TypeAgrement> specification = TypeAgrementSpecifications.deleted(false).and(TypeAgrementSpecifications.libelle(request.getLibelle()));
            Optional<TypeAgrement> TypeAgrement1 = findOne(specification);
            if(TypeAgrement1.isPresent()&& !Objects.equals(TypeAgrement1.get().getId(), id)) {
                throw new ActiviteAlreadyExist("", request.getLibelle());
            }
            typeAgrement.setLibelle(request.getLibelle());
        }
        RefType organismeDeDeliveranceAgrements=refTypeService.findById(request.getOrganismeDeDeliveranceAgrementsId());
        if (!organismeDeDeliveranceAgrements.getRefCode().equals(RefTypes.ORGANISME_DE_DELIVRANCE_AGREMENTS.name())) {
            throw new WrongType("");
        }
        typeAgrement.setOrganismeDeDeliveranceAgrements(organismeDeDeliveranceAgrements);
        return repository.save(typeAgrement);
    }

    @Override
    public void delete(Integer id) throws TypeAgrementNotFound {
        findById(id);
        repository.softDelete(id);
    }
}
