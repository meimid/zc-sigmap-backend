package com.sigmap.common.ref_params;


import com.sigmap.common.ref_params.exceptions.*;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.ref_params.request.RefParamRequest;
import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeService;

import java.util.List;
import java.util.Optional;

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
        Specification<RefParam> spec = RefParamSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<RefParam> findAll(Specification<RefParam> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<RefParam> findOne(Specification<RefParam> specification) {
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
        Specification<RefParam> specification = RefParamSpecifications.id(id).and(RefParamSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public RefParam findById(Integer id) throws RefParamNotFound {
        Specification<RefParam> specification = RefParamSpecifications.id(id)
                .and(RefParamSpecifications.deleted(false));
        return findOne(specification)
                .orElseThrow(() -> new RefParamNotFound("", List.of(id)));
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
            refParam.setRefType(refType);
        }
        return save(refParam);
    }

    @Override
    public RefParam update(Integer id,RefParamRequest request) throws RefParamNotFound, RefTypeNotFound {
        RefParam refParam = findById(id);
        RefParamMapper.INSTANCE.updateFromRequest(request, refParam);
        if(request.getRefTypeId()!=null) {
            RefType refType = refTypeService.findById(request.getRefTypeId());
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
            HasAssociatedAgpm,HasAssociatedRefRealisation{
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


        repository.softDelete(id);
    }

    @Override
    public List<RefParam> findByRefType(Integer refTpeId) throws RefTypeNotFound {
        refTypeService.findById(refTpeId);
       return repository.findByRefType(refTpeId);
    }

    @Override
    public RefParam findByIdAndRefTypeId(Integer refPramId, Integer refTypeId) throws RefParamNotFound  {
        refTypeService.findById(refTypeId);
        RefParam refParam=repository.findByIdAndRefType_Id(refPramId, refTypeId).orElseThrow(() -> new RefParamNotFound("",List.of(refPramId)));
        if(refParam.getIsDeleted()){
            throw new RefParamNotFound("",List.of(refPramId));
        }
        return  refParam;
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
    public RefParam createUnderRefType(Integer refTpeId, RefParamRequest request) throws RefTypeNotFound {
        RefType refType = refTypeService.findById(refTpeId);
        RefParam refParam = RefParamMapper.INSTANCE.map(request);
        refParam.setRefType(refType);
        return save(refParam);
    }

}
