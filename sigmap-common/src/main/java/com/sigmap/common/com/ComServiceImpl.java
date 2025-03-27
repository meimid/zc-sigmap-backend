package com.sigmap.common.com;
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

@Service
@RequiredArgsConstructor
public class ComServiceImpl implements ComService{
    private final ComRepository repository;
    private final RefParamService refParamService;
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
        RefParam refParam=refParamService.findById(request.getRefParamId()).orElseThrow();
        Com com = new Com();
        com.setNom(request.getNom());
        com.setDecretCreation(request.getDecretCreation());
        com.setResponsable(request.getResponsable());
        com.setSpecial(request.getSpecial());
        com.setAncrage(request.getAncrage());
        com.setContact(request.getContact());
        com.setAdresse(request.getAdresse());
        com.setMembres(request.getMembres());
        com.setRefParam(refParam);
        return save(com);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }
}
