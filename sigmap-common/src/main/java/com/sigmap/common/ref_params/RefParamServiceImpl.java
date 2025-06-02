package com.sigmap.common.ref_params;

import com.sigmap.common.ref_params.exceptions.*;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.ref_params.request.RefParamRequest;
import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeService;
import java.util.List;
import java.util.Optional;
import com.sigmap.common.ref_types.RefTypeSpecifications;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Service implementation of {@link RefParamService}
 */
@Service
public class RefParamServiceImpl implements RefParamService {
    private final RefParamRepository repository;
    private final RefTypeService refTypeService;

    /**
     * Constructor.
     *
     * @param repository     refParam repository
     * @param refTypeService refType service
     */
    public RefParamServiceImpl(RefParamRepository repository, RefTypeService refTypeService) {
        this.repository = repository;
        this.refTypeService = refTypeService;
    }

    @Override
    public List<RefParam> findAll() {
        Specification<RefParam> spec = RefParamSpecifications.systemType(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<RefParam> findAll(Specification<RefParam> spec, Pageable pageable) {
        Specification<RefParam> systemTypeSpec = RefParamSpecifications.systemType(false);
        Specification<RefParam> combinedSpec;
        if (spec != null) {
            combinedSpec = spec.and(systemTypeSpec);
        } else {
            combinedSpec = systemTypeSpec;
        }
        return repository.findAll(combinedSpec, pageable);

    }

    @Override
    public Optional<RefParam> findOne(Specification<RefParam> specification) {
        Optional<RefParam> ref = repository.findOne(specification);
        if (ref.isPresent() &&ref.get().getRefType().getSystemType()) {
            return Optional.empty();
        }
        return repository.findOne(specification);

    }

    @Override
    public Page<RefParam> findAllDeleted(Pageable pageable) {
        Specification<RefParam> spec = RefParamSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<RefParam> findAllDeleted(Specification<RefParam> spec, Pageable pageable) {
        spec = RefParamSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<RefParam> findDeletedById(Integer id) {
        Specification<RefParam> specification = RefParamSpecifications.id(id).and(RefParamSpecifications.systemType(true));
        return findOne(specification);
    }

    @Override
    public RefParam findById(Integer id) throws RefParamNotFound {
        Specification<RefParam> specification = RefParamSpecifications.id(id)
                .and(RefParamSpecifications.systemType(false));
        RefParam ref= findOne(specification)
                .orElseThrow(() -> new RefParamNotFound("", List.of(id)));
        if(ref.getRefType().getSystemType()!=null&&ref.getRefType().getSystemType()) {
            throw new RefParamNotFound("", List.of(id));
        }
        return ref;
    }

    @Override
    public RefParam save(RefParam refParam) {
        return repository.save(refParam);
    }

    @Override
    public RefParam create(RefParamRequest request) throws RefTypeNotFound {
        RefParam refParam = RefParamMapper.INSTANCE.map(request);
        if(request.getRefTypeId()!=null) {
            RefType refType = refTypeService.findById(request.getRefTypeId());
            if(refType.getSystemType() == null || refType.getSystemType() | refType.getIsDeleted()) {
                throw new RefTypeNotFound("",request.getRefTypeId());
            }
            refParam.setRefType(refType);

        }
        return save(refParam);
    }

    @Override
    public RefParam update(Integer id,RefParamRequest request) throws RefParamNotFound, RefTypeNotFound {
        RefParam refParam = findById(id);
        if(refParam.getRefType().getSystemType()) {
            throw new RefParamNotFound("", List.of(id));
        }
        RefParamMapper.INSTANCE.updateFromRequest(request, refParam);
        if(request.getRefTypeId()!=null) {
            RefType refType = refTypeService.findById(request.getRefTypeId());
            if(refType.getSystemType() == null ||refType.getSystemType()|refType.getIsDeleted()) {
                throw new RefTypeNotFound("",request.getRefTypeId());
            }
            refParam.setRefType(refType);
        }

        return save(refParam);
    }

    @Override
    public void delete(Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress,
            HasAssociatedPpm, HasAssociatedPaa,
            HasAssociatedAgpm, HasAssociatedRefRealisation, RefParamNotFound {
        boolean hasAssociatedCom = repository.hasAssociatedCom(id);
        if (hasAssociatedCom) {
            throw new RefParamHasAssociatedCom("",id);
        }
        boolean hasAssociatedCompteBancaire = repository.hasAssociatedCompteBancaire(id);
        if (hasAssociatedCompteBancaire) {
            throw new RefParamHasAssociatedCompteBancaire("",id);
        }

        boolean hasAssociatedSociete = repository.hasAssociatedSociete(id);
        if (hasAssociatedSociete) {
            throw new RefParamHasAssociatedSociete("",id);
        }
        boolean hasAssociatedSocieteAddress = repository.hasAssociatedSocieteAddress(id);
        if (hasAssociatedSocieteAddress) {
            throw new RefParamHasAssociatedSocieteAddress("",id);
        }
        boolean hasAssociatedPpm = repository.hasAssociatedPpm(id);
        if (hasAssociatedPpm) {
            throw new HasAssociatedPpm("",id);
        }
        boolean hasAssociatedPaa = repository.hasAssociatedPaa(id);
        if (hasAssociatedPaa) {
            throw new HasAssociatedPaa("",id);
        }
        boolean hasAssociatedAgpm = repository.hasAssociatedAgpm(id);
        if (hasAssociatedAgpm) {
            throw new HasAssociatedAgpm("",id);
        }
        boolean hasAssociatedRealisation = repository.hasAssociatedRealisation(id);
        if (hasAssociatedRealisation) {
            throw new HasAssociatedRefRealisation("",id);
        }
         findById(id);

        repository.softDelete(id);
    }

    @Override
    public List<RefParam> findByRefType(Integer refTpeId) throws RefTypeNotFound {
        Specification<RefType> systemTypeSpec = RefTypeSpecifications.id(refTpeId).and(RefTypeSpecifications.systemType());
        refTypeService.findOne(systemTypeSpec).orElseThrow(() -> new RefTypeNotFound("",refTpeId));
       return repository.findByRefType(refTpeId);
    }

    @Override
    public RefParam findByIdAndRefTypeId(Integer refPramId, Integer refTypeId) throws RefParamNotFound,RefTypeNotFound  {
        Specification<RefType> systemTypeSpec = RefTypeSpecifications.id(refTypeId).and(RefTypeSpecifications.systemType());
        refTypeService.findOne(systemTypeSpec).orElseThrow(() -> new RefTypeNotFound("",refTypeId));
       Specification<RefParam> refParamSpecification= RefParamSpecifications.id(refPramId).and(RefParamSpecifications.systemType(false));
       findOne(refParamSpecification).orElseThrow(()-> new RefParamNotFound("", List.of(refPramId)));
        return  repository.findByIdAndRefType_Id(refPramId, refTypeId).orElseThrow(() -> new RefParamNotFound("",List.of(refPramId)));
    }

    @Override
    public RefParam UpdateRefParamByRefTypeId(Integer refParamId, Integer refTypeId,RefParamRequest request) throws RefParamNotFound {
        return update(findByIdAndRefTypeId( refParamId, refTypeId).getId(),request);
    }

    @Override
    public void deleteByIdAndRefTypeId(Integer refParamId, Integer refTypeId) throws RefParamNotFound {
        delete(findByIdAndRefTypeId( refParamId, refTypeId).getId());
    }

    @Override
    public RefParam createUnderRefType(Integer refTypeId, RefParamRequest request) throws RefTypeNotFound {
        Specification<RefType> systemTypeSpec = RefTypeSpecifications.id(refTypeId).and(RefTypeSpecifications.systemType());
        RefType refType= refTypeService.findOne(systemTypeSpec).orElseThrow(() -> new RefTypeNotFound("",refTypeId));
        RefParam refParam = RefParamMapper.INSTANCE.map(request);
        refParam.setRefType(refType);
        return save(refParam);
    }

}
