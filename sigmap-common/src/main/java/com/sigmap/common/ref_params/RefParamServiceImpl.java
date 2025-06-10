package com.sigmap.common.ref_params;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmRepository;
import com.sigmap.common.agpm.AgpmSpecifications;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComRepository;
import com.sigmap.common.com.ComSpecifications;
import com.sigmap.common.compte_bancaire.CompteBancaire;
import com.sigmap.common.compte_bancaire.CompteBancaireRepository;
import com.sigmap.common.compte_bancaire.CompteBancaireSpecifications;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaRepository;
import com.sigmap.common.paas.PaaSpecifications;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.realisations.RealisationSpecifications;
import com.sigmap.common.ref_params.exceptions.*;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.ref_params.request.RefParamRequest;
import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeService;
import java.util.List;
import java.util.Optional;
import com.sigmap.common.ref_types.RefTypeSpecifications;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteRepository;
import com.sigmap.common.societe.SocieteSpecifications;
import com.sigmap.common.societe_address.SocieteAddress;
import com.sigmap.common.societe_address.SocieteAddressRepository;
import com.sigmap.common.societe_address.SocieteAddressSpecifications;
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
    private final AgpmRepository agpmRepository;
    private final CompteBancaireRepository compteBancaireRepository;
    private final SocieteRepository societeRepository;
    private final RealisationRepository realisationRepository;
    private final PpmRepository ppmRepository;
    private final PaaRepository paaRepository;
    private final SocieteAddressRepository societeAddressRepository;
    private final ComRepository comRepository;

    /**
     * Constructor.
     *
     * @param repository     refParam repository
     * @param refTypeService refType service
     */

    public RefParamServiceImpl(RefParamRepository repository,
                               RefTypeService refTypeService,
                               AgpmRepository agpmRepository,
                               CompteBancaireRepository compteBancaireRepository,
                               SocieteRepository societeRepository,
                               RealisationRepository realisationRepository,
                               PpmRepository ppmRepository,
                               PaaRepository paaRepository,
                               SocieteAddressRepository societeAddressRepository,
                               ComRepository comRepository) {
        this.repository = repository;
        this.refTypeService = refTypeService;
        this.agpmRepository = agpmRepository;
        this.compteBancaireRepository = compteBancaireRepository;
        this.societeRepository = societeRepository;
        this.realisationRepository = realisationRepository;
        this.ppmRepository = ppmRepository;
        this.paaRepository = paaRepository;
        this.societeAddressRepository = societeAddressRepository;
        this.comRepository = comRepository;
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

         findById(id);
        Specification<Com> comSpec = Specification.where(
                ComSpecifications.deleted(false)
                        .and(ComSpecifications.hasComTypeId(id))
        );
        boolean hasAssociatedCom = comRepository.count(comSpec) > 0;
        if (hasAssociatedCom) {
            throw new RefParamHasAssociatedCom("", id);
        }

        Specification<CompteBancaire> compteBancaireSpec = Specification.where(
                CompteBancaireSpecifications.deleted(false)
                        .and(CompteBancaireSpecifications.hasPaysId(id))
        );
        boolean hasAssociatedCompteBancaire = compteBancaireRepository.count(compteBancaireSpec) > 0;
        if (hasAssociatedCompteBancaire) {
            throw new RefParamHasAssociatedCompteBancaire("", id);
        }

        Specification<Societe> societeSpec = Specification.where(
                SocieteSpecifications.deleted(false)
                        .and(SocieteSpecifications.hasPaysIdOrTypeId(id))
        );
        boolean hasAssociatedSociete = societeRepository.count(societeSpec) > 0;
        if (hasAssociatedSociete) {
            throw new RefParamHasAssociatedSociete("", id);
        }

        Specification<SocieteAddress> societeAddressSpec = Specification.where(
                SocieteAddressSpecifications.deleted(false)
                        .and(SocieteAddressSpecifications.hasTypeId(id))
        );
        boolean hasAssociatedSocieteAddress = societeAddressRepository.count(societeAddressSpec) > 0;
        if (hasAssociatedSocieteAddress) {
            throw new RefParamHasAssociatedSocieteAddress("", id);
        }

        Specification<Ppm> ppmSpec = Specification.where(
                PpmSpecifications.deleted(false)
                        .and(PpmSpecifications.hasStatusId(id))
                        .and(PpmSpecifications.statusNotDeleted())
        );
        boolean hasAssociatedPpm = ppmRepository.count(ppmSpec) > 0;
        if (hasAssociatedPpm) {
            throw new HasAssociatedPpm("", id);
        }

        Specification<Paa> paaSpec = Specification.where(
                PaaSpecifications.deleted(false)
                        .and(PaaSpecifications.hasStatusId(id))
                        .and(PaaSpecifications.statusNotDeleted())
        );
        boolean hasAssociatedPaa = paaRepository.count(paaSpec) > 0;
        if (hasAssociatedPaa) {
            throw new HasAssociatedPaa("", id);
        }

        Specification<Agpm> agpmSpec = Specification.where(
                AgpmSpecifications.deleted(false)
                        .and(AgpmSpecifications.hasStatusId(id))
                        .and(AgpmSpecifications.statusNotDeleted())
        );
        boolean hasAssociatedAgpm = agpmRepository.count(agpmSpec) > 0;
        if (hasAssociatedAgpm) {
            throw new HasAssociatedAgpm("", id);
        }

        Specification<Realisation> realisationSpec = Specification.where(
                RealisationSpecifications.deleted(false)
                        .and(RealisationSpecifications.hasRefParamId(id))
        );
        boolean hasAssociatedRealisation = realisationRepository.count(realisationSpec) > 0;
        if (hasAssociatedRealisation) {
            throw new HasAssociatedRefRealisation("", id);
        }

        boolean hasAssociatedDynaDoc = repository.hasAssociatedDynaDoc(id);
        if (hasAssociatedDynaDoc) {
            throw new HasAssociatedDynaDoc(id);
        }

        boolean hasAssociatedDynaField = repository.hasAssociatedDynaField(id);
        if (hasAssociatedDynaField) {
            throw new HasAssociatedDynaField(id);
        }

        boolean hasAssociatedDynaFieldDetail = repository.hasAssociatedDynaFieldDetail(id);
        if (hasAssociatedDynaFieldDetail) {
            throw new HasAssociatedDynaFieldDetail(id);
        }

        repository.softDelete(id);
    }

    @Override
    public List<RefParam> findByRefType(Integer refTypeId) throws RefTypeNotFound {
        refTypeService.findById(refTypeId);
        Specification<RefType> refTypeSpec = RefTypeSpecifications.id(refTypeId)
                .and(RefTypeSpecifications.systemType());
        refTypeService.findOne(refTypeSpec).orElseThrow(() -> new RefTypeNotFound("", refTypeId));

        Specification<RefParam> refParamSpec = Specification.where(
                RefParamSpecifications.hasRefTypeId(refTypeId)
                        .and(RefParamSpecifications.deleted(false))
        );

        return repository.findAll(refParamSpec);
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
