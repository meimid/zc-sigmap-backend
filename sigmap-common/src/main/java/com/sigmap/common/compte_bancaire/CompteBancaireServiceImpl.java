package com.sigmap.common.compte_bancaire;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.compte_bancaire.exception.CompteBancaireNotFound;
import com.sigmap.common.compte_bancaire.mapper.CompteBancaireMapper;
import com.sigmap.common.compte_bancaire.request.CompteBancaireRequest;
import com.sigmap.common.compte_bancaire.request.CompteBancaireRequestDTO;
import com.sigmap.common.compte_bancaire.request.CompteBancaireUnderSocieteRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
@Service
public class CompteBancaireServiceImpl implements CompteBancaireService {
    private final CompteBancaireRepository repository;
    private final RefParamService refParamService;
    private final SocieteService societeService;

    public CompteBancaireServiceImpl(CompteBancaireRepository repository, RefParamService refParamService, SocieteService societeService) {
        this.repository = repository;
        this.refParamService = refParamService;
        this.societeService = societeService;
    }

    @Override
    public List<CompteBancaire> findAll() {
        Specification<CompteBancaire> spec = CompteBancaireSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<CompteBancaire> findAll(Specification<CompteBancaire> CompteBancaire, Pageable pageable) {
        return repository.findAll(CompteBancaire, pageable);
    }

    @Override
    public Page<CompteBancaire> findAllDeleted(Specification<CompteBancaire> compteBancaire, Pageable pageable) {
        compteBancaire = CompteBancaireSpecifications.deleted(true);
        return repository.findAll(compteBancaire, pageable);
    }

    @Override
    public Page<CompteBancaire> findAllDeleted(Pageable pageable) {
        Specification<CompteBancaire> compteBancaire = CompteBancaireSpecifications.deleted(true);
        return repository.findAll(compteBancaire, pageable);
    }

    @Override
    public Optional<CompteBancaire> findDeletedById(Integer id) {
        Specification<CompteBancaire> specification = CompteBancaireSpecifications.id(id).and(CompteBancaireSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<CompteBancaire> findOne(Specification<CompteBancaire> specification) {
        return repository.findOne(specification);
    }

    @Override
    public CompteBancaire findById(Integer id) throws CompteBancaireNotFound {
        Specification<CompteBancaire> specification = CompteBancaireSpecifications.id(id)
                .and(CompteBancaireSpecifications.deleted(false));
        return findOne(specification)
                .orElseThrow(() -> new CompteBancaireNotFound("", List.of(id)));
    }

    @Override
    public CompteBancaire save(CompteBancaire compteBancaire) {
        return repository.save(compteBancaire);
    }

    @Override
    public CompteBancaire create(CompteBancaireRequestDTO request) throws RefParamNotFound, SocieteNotFound {
        RefParam pays=refParamService.findById(request.getPaysId());
        Societe societe=societeService.findById(request.getSocieteId());
        CompteBancaire compteBancaire= CompteBancaireMapper.INSTANCE.map(request);
        compteBancaire.setPays(pays);
        compteBancaire.setSociete(societe);
        return repository.save(compteBancaire);
    }

    @Override
    public CompteBancaire update(Integer id, CompteBancaireRequestDTO request) throws CompteBancaireNotFound, RefParamNotFound, SocieteNotFound{
        CompteBancaire compteBancaire=repository.findById(id)
                .orElseThrow(() -> new CompteBancaireNotFound("",List.of(id)));
        RefParam pays=refParamService.findById(request.getPaysId());
        Societe societe=societeService.findById(request.getSocieteId());
        CompteBancaireMapper.INSTANCE.updateFromRequest(request, compteBancaire);
        compteBancaire.setPays(pays);
        compteBancaire.setSociete(societe);
        return repository.save(compteBancaire);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);

    }

    @Override
    public CompteBancaire createUnderSociete(Integer societeId, CompteBancaireUnderSocieteRequest request) throws RefParamNotFound, SocieteNotFound {
        RefParam pays=refParamService.findById(request.getPaysId());
        Societe societe=societeService.findById(societeId);
        CompteBancaire compteBancaire= CompteBancaireMapper.INSTANCE.mapFromUnderUnderSocieteRequest(request);
        compteBancaire.setPays(pays);
        compteBancaire.setSociete(societe);
        return repository.save(compteBancaire);    }

    @Override
    public Page<CompteBancaire> findCompteBancaireListBySocieteId(Integer societeId,Pageable pageable) throws SocieteNotFound {
        societeService.findById(societeId);
        return repository.findCompteBancaireBySocieteId(societeId,pageable);
    }
}
