package com.sigmap.common.ref_params;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.AcSpecifications;
import com.sigmap.common.activite.Activite;
import com.sigmap.common.activite.ActiviteRepository;
import com.sigmap.common.activite.ActiviteSpecifications;
import com.sigmap.common.adresse.AdressSpecifications;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.adresse.AdresseRespository;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComRepository;
import com.sigmap.common.com.ComSpecifications;
import com.sigmap.common.compte_bancaire.CompteBancaire;
import com.sigmap.common.compte_bancaire.CompteBancaireRepository;
import com.sigmap.common.compte_bancaire.CompteBancaireSpecifications;
import com.sigmap.common.config.RefTypes;
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
import com.sigmap.common.ref_types.exceptions.RefTypeIdNotFound;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteRepository;
import com.sigmap.common.societe.SocieteSpecifications;
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
    private final CompteBancaireRepository compteBancaireRepository;
    private final SocieteRepository societeRepository;
    private final RealisationRepository realisationRepository;
    private final PpmRepository ppmRepository;
    private final PaaRepository paaRepository;
    private final ComRepository comRepository;
    private final AcRepository acRepository;
    private final  AdresseRespository  adresseRespository;
    private final ActiviteRepository activiteRepository;

    /**
     * Constructor.
     *
     * @param repository     refParam repository
     * @param refTypeService refType service
     */

    public RefParamServiceImpl(RefParamRepository repository,
                               RefTypeService refTypeService,
                               CompteBancaireRepository compteBancaireRepository,
                               SocieteRepository societeRepository,
                               RealisationRepository realisationRepository,
                               PpmRepository ppmRepository,
                               PaaRepository paaRepository,
                               ComRepository comRepository,
                               AcRepository acRepository, AdresseRespository adresseRespository, ActiviteRepository activiteRepository
    ) {
        this.repository = repository;
        this.refTypeService = refTypeService;
        this.compteBancaireRepository = compteBancaireRepository;
        this.societeRepository = societeRepository;
        this.realisationRepository = realisationRepository;
        this.ppmRepository = ppmRepository;
        this.paaRepository = paaRepository;
        this.comRepository = comRepository;
        this.acRepository = acRepository;
        this.adresseRespository = adresseRespository;
        this.activiteRepository = activiteRepository;
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
                .and(RefParamSpecifications.systemType(false)).and(RefParamSpecifications.deleted(false));
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
        Specification<RefParam> specification = RefParamSpecifications.systemType(false).and(RefParamSpecifications.code(request.getCode())).and(RefParamSpecifications.deleted(false));
        List<RefParam> refParamUnique=findListRefParamSpecification(specification);
        if (!refParamUnique.isEmpty()) {
        throw new RefParamAlredyExist("",request.getCode());
        }
        RefParam refParam = RefParamMapper.INSTANCE.map(request);
        if(request.getRefTypeId()!=null) {
            RefType refType = refTypeService.findById(request.getRefTypeId());
            if(refType.getSystemType() == null || refType.getSystemType() | refType.getIsDeleted()) {
                throw new RefTypeIdNotFound("",request.getRefTypeId());
            }
            refParam.setRefType(refType);

        }
        return save(refParam);
    }

    @Override
    public RefParam update(Integer id,RefParamRequest request) throws RefParamNotFound, RefTypeNotFound {
        RefParam refParam = findById(id);
        Specification<RefParam> specification = RefParamSpecifications.systemType(false).and(RefParamSpecifications.code(request.getCode())).and(RefParamSpecifications.deleted(false));
        List<RefParam> refParams=findListRefParamSpecification(specification);
        if (!refParams.isEmpty()) {
            for(RefParam refParam1: refParams) {
                if(!refParam1.getId().equals(id)) {
                    throw new RefParamAlredyExist("",request.getCode());
                }
            }
        }
        if(refParam.getRefType().getSystemType()) {
            throw new RefParamNotFound("", List.of(id));
        }
        RefParamMapper.INSTANCE.updateFromRequest(request, refParam);
        if(request.getRefTypeId()!=null) {
            RefType refType = refTypeService.findById(request.getRefTypeId());
            if(refType.getSystemType() == null ||refType.getSystemType()|refType.getIsDeleted()) {
                throw new RefTypeIdNotFound("",request.getRefTypeId());
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
         Specification<Ac> acSpec=Specification.where(AcSpecifications.hasTutleId(id).and(AcSpecifications.deleted(false)));
         boolean hasAAssociatedAc=acRepository.count(acSpec)>0;
        if (hasAAssociatedAc) {
            throw new RefParamHasAssociatedAc("", id);
        }

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



        Specification<Ppm> ppmSpec = Specification.where(
                PpmSpecifications.deleted(false)
                        .and(PpmSpecifications.hasModePassationId(id))
                        .and(PpmSpecifications.modePassationNotDeleted())
        );
        boolean hasAssociatedPpm = ppmRepository.count(ppmSpec) > 0;
        if (hasAssociatedPpm) {
            throw new HasAssociatedPpm("", id);
        }

        Specification<Paa> paaSpec = Specification.where(
                PaaSpecifications.deleted(false)
                        .and(PaaSpecifications.hasModePassationId(id))
                        .and(PaaSpecifications.modePassationNotDeleted())
        );
        boolean hasAssociatedPaa = paaRepository.count(paaSpec) > 0;
        if (hasAssociatedPaa) {
            throw new HasAssociatedPaa("", id);
        }
/*
        Specification<Agpm> agpmSpec = Specification.where(
                AgpmSpecifications.deleted(false)
                        .and(AgpmSpecifications.hasStatusId(id))
                        .and(AgpmSpecifications.statusNotDeleted())
        );
        boolean hasAssociatedAgpm = agpmRepository.count(agpmSpec) > 0;
        if (hasAssociatedAgpm) {
            throw new HasAssociatedAgpm("", id);
        }*/

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
        Specification<Adresse> adresseSpec = Specification.where(
                AdressSpecifications.deleted(false)
                        .and(AdressSpecifications.hasPaysId(id))
        );
        boolean hasAddresse = adresseRespository.count(adresseSpec) > 0;
        if (hasAddresse) {
            throw new HasAssociatedAdress("", id);
        }
        Specification<Activite> spec = Specification.where(ActiviteSpecifications.hasActiviteId(id).and(ActiviteSpecifications.deleted(false)));

        boolean hasAssociatedActivite = activiteRepository.count(spec) > 0;
        if (hasAssociatedActivite) {
            throw new HasAssociatedActivite("", id);
        }

        repository.softDelete(id);
    }

    @Override
    public List<RefParam> findByRefType(RefTypes refTypeCode) throws RefTypeNotFound {
        refTypeService.findByCode(refTypeCode);
        Specification<RefType> refTypeSpec = RefTypeSpecifications.code(refTypeCode.name())
                .and(RefTypeSpecifications.systemType());
        refTypeService.findOne(refTypeSpec).orElseThrow(() -> new RefTypeNotFound("", refTypeCode));

        Specification<RefParam> refParamSpec = Specification.where(
                RefParamSpecifications.hasRefTypeCode(refTypeCode)
                        .and(RefParamSpecifications.deleted(false))
        );

        return repository.findAll(refParamSpec);
    }

    @Override
    public RefParam findByIdAndRefTypeId(Integer refPramId, RefTypes refTypeCode) throws RefParamNotFound,RefTypeNotFound  {
        Specification<RefType> systemTypeSpec = RefTypeSpecifications.code(refTypeCode.name()).and(RefTypeSpecifications.systemType());
        refTypeService.findOne(systemTypeSpec).orElseThrow(() -> new RefTypeNotFound("",refTypeCode));
       Specification<RefParam> refParamSpecification= RefParamSpecifications.id(refPramId).and(RefParamSpecifications.systemType(false));
       findOne(refParamSpecification).orElseThrow(()-> new RefParamNotFound("", List.of(refPramId)));
        return  repository.findByIdAndRefType_Id(refPramId, refTypeCode.name()).orElseThrow(() -> new RefParamNotFound("",List.of(refPramId)));
    }

    @Override
    public RefParam UpdateRefParamByRefTypeId(Integer refParamId, RefTypes refTypeCode,RefParamRequest request) throws RefParamNotFound {
        return update(findByIdAndRefTypeId( refParamId, refTypeCode).getId(),request);
    }

    @Override
    public void deleteByIdAndRefTypeId(Integer refParamId, RefTypes refTypeCode) throws RefParamNotFound {
        delete(findByIdAndRefTypeId( refParamId, refTypeCode).getId());
    }

    @Override
    public RefParam createUnderRefType(RefTypes refTypeCode, RefParamRequest request) throws RefTypeNotFound {
        Specification<RefType> systemTypeSpec = RefTypeSpecifications.code(refTypeCode.name()).and(RefTypeSpecifications.systemType());
        RefType refType= refTypeService.findOne(systemTypeSpec).orElseThrow(() -> new RefTypeNotFound("",refTypeCode));
        RefParam refParam = RefParamMapper.INSTANCE.map(request);
        refParam.setRefType(refType);
        return save(refParam);
    }

    @Override
    public List<RefParam> findListRefParamSpecification(Specification<RefParam> refParamSpecification) {
        return repository.findAll(refParamSpecification);
    }

    @Override
    public List<RefParam> findByRefTypeWithoutSystemType(RefTypes refTypeCode) {
        refTypeService.findByCode(refTypeCode);
        Specification<RefType> refTypeSpec = RefTypeSpecifications.code(refTypeCode.name());

        refTypeService.findOne(refTypeSpec).orElseThrow(() -> new RefTypeNotFound("", refTypeCode));

        Specification<RefParam> refParamSpec = Specification.where(
                RefParamSpecifications.hasRefTypeCode(refTypeCode)
                        .and(RefParamSpecifications.deleted(false))
        );

        return repository.findAll(refParamSpec);
    }

    @Override
    public RefParam findByIdWithoutSystemType(Integer id) throws RefParamNotFound {
        Specification<RefParam> specification = RefParamSpecifications.id(id)
                .and(RefParamSpecifications.deleted(false));
        RefParam ref= findOneWithoutSystemType(specification)
                .orElseThrow(() -> new RefParamNotFound("", List.of(id)));
        return ref;
    }

    @Override
    public Optional<RefParam> findOneWithoutSystemType(Specification<RefParam> specification) {
        return repository.findOne(specification);
    }

}
