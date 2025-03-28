package com.sigmap.common.com;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.com.request.ComRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.RefParamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service

public class ComServiceImpl implements ComService{
    private final ComRepository repository;
    private final RefParamService refParamService;
    private final AcService acService;

    public ComServiceImpl(ComRepository repository, RefParamService refParamService, AcService acService) {
        this.repository = repository;
        this.refParamService = refParamService;
        this.acService = acService;
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
        RefParam comType=refParamService.findById(request.getComTypeId()).orElseThrow();
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
            for (Integer i : request.getAcIds()) {

                Ac ac = acService.findById(i).orElseThrow(() -> new RuntimeException("Ac not found: " + i));
                if (ac.getIsDeleted()!=null) { // Assuming Ac extends BaseEntity with isDeleted
                    throw new RuntimeException("Ac with ID " + i + " is deleted and cannot be associated");
                }
                com.getAcs().add(ac);
            }
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
            com.getAcs().clear();
            for (Integer i : request.getAcIds()) {

                Ac ac = acService.findById(i).orElseThrow(() -> new RuntimeException("Ac not found: " + i));
                if (ac.getIsDeleted()!=null) { // Assuming Ac extends BaseEntity with isDeleted
                    throw new RuntimeException("Ac with ID " + i + " is deleted and cannot be associated");
                }
                com.getAcs().add(ac);
            }
        }
        return repository.save(com);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }
}
