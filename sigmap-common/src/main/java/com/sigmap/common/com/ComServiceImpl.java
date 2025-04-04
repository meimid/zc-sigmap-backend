package com.sigmap.common.com;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.com.request.ComRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service

public class ComServiceImpl implements ComService{
    private final ComRepository repository;
    private final RefParamService refParamService;
    private final AcRepository acRepository;

    public ComServiceImpl(ComRepository repository, RefParamService refParamService, AcRepository acRepository) {
        this.repository = repository;
        this.refParamService = refParamService;
        this.acRepository = acRepository;
    }

    @Override
    public List<Com> findAll() {
        Specification<Com> spec = ComSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Com> findAll(Specification<Com> com, Pageable pageable) {
        return repository.findAll(com, pageable);
    }

    @Override
    public Page<Com> findAllDeleted(Specification<Com> com, Pageable pageable) {
        com = ComSpecifications.deleted(true);
        return repository.findAll(com, pageable);
    }

    @Override
    public Page<Com> findAllDeleted(Pageable pageable) {
        Specification<Com> com = ComSpecifications.deleted(true);
        return repository.findAll(com, pageable);
    }

    @Override
    public Optional<Com> findDeletedById(Integer id) {
        Specification<Com> specification = ComSpecifications.id(id).and(ComSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Com> findOne(Specification<Com> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Com> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Com save(Com com) {

 return repository.save(com);
    }

    @Override
    public Com create(ComRequest request) {
        RefParam comType=refParamService.findById(request.getComTypeId()).orElseThrow(() -> new RuntimeException("Societe not found: " +request.getComTypeId()));
        Com com = new Com();
        com.setNom(request.getNom());
        com.setDecretCreation(request.getDecretCreation());
        com.setResponsable(request.getResponsable());
        com.setSpecial(request.getSpecial());
        com.setAncrage(request.getAncrage());
        com.setContact(request.getContact());
        com.setAdresse(request.getAdresse());
        com.setMembres(request.getMembres());
        if (comType.getId() != null) {
            com.setComType(comType);
        } else {
            com.setComType(null);
        }
        if (request.getAcIds() != null && !request.getAcIds().isEmpty()) {
            List<Ac> acs = acRepository.findAllById(request.getAcIds());

            // Validate that we found all requested Com entities
            if (acs.size() != request.getAcIds().size()) {
                throw new IllegalArgumentException("One or more Com IDs were not found");

            }
            com.setAcs(acs);
        }
        return repository.save(com);
    }

    @Override
    public Com update(ComRequest request) {
        Com com = repository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Com not found: " + request.getId()));
        RefParam comType=refParamService.findById(request.getComTypeId()).orElseThrow(()-> new RuntimeException("comType not found: " + request.getComTypeId()));
        com.setNom(request.getNom());
        com.setDecretCreation(request.getDecretCreation());
        com.setResponsable(request.getResponsable());
        com.setSpecial(request.getSpecial());
        com.setAncrage(request.getAncrage());
        com.setContact(request.getContact());
        com.setAdresse(request.getAdresse());
        com.setMembres(request.getMembres());
        if (comType.getId() != null) {
            com.setComType(comType);
        } else {
            com.setComType(null);
        }
        if (request.getAcIds() != null && !request.getAcIds().isEmpty()) {
            List<Ac> acs = acRepository.findAllById(request.getAcIds());

            if (acs.size() != request.getAcIds().size()) {
                throw new IllegalArgumentException("One or more Com IDs were not found");

            }
            com.setAcs(acs);
        }
        return repository.save(com);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }
}
