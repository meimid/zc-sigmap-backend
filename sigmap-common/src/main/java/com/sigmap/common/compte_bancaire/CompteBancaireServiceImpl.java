package com.sigmap.common.compte_bancaire;
import com.sigmap.common.compte_bancaire.mapper.CompteBancaireMapper;
import com.sigmap.common.compte_bancaire.request.CompteBancaireRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
    public Optional<CompteBancaire> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public CompteBancaire save(CompteBancaire compteBancaire) {
        return repository.save(compteBancaire);
    }

    @Override
    public CompteBancaire create(CompteBancaireRequest request) {
        RefParam pays=refParamService.findById(request.getPaysId()).orElseThrow(() -> new RuntimeException("pays not found: " + request.getPaysId()));
        Societe societe=societeService.findById(request.getSocieteId()).orElseThrow(() -> new RuntimeException("societe not found: " + request.getSocieteId()));
        CompteBancaire compteBancaire=new CompteBancaire();
        compteBancaire= CompteBancaireMapper.INSTANCE.map(request);
        compteBancaire.setPays(pays);
        compteBancaire.setSociete(societe);
        return repository.save(compteBancaire);
    }

    @Override
    public CompteBancaire update(CompteBancaireRequest request) {
      CompteBancaire compteBancaire=repository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Com not found: " + request.getId()));
        RefParam pays=refParamService.findById(request.getPaysId()).orElseThrow(() -> new RuntimeException("pays not found: " + request.getPaysId()));
        Societe societe=societeService.findById(request.getSocieteId()).orElseThrow(() -> new RuntimeException("societe not found: " + request.getSocieteId()));
        CompteBancaireMapper.INSTANCE.updateFromRequest(request, compteBancaire);
        compteBancaire.setPays(pays);
        compteBancaire.setSociete(societe);
        return repository.save(compteBancaire);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);

    }
}
