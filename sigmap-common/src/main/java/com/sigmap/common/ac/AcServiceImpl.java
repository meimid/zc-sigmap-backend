package com.sigmap.common.ac;

import com.sigmap.common.GlobalException.BadRequest;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.request.AcRequest;
import com.sigmap.common.ac.response.AcResponse;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComRepository;
import com.sigmap.common.com.exception.ComNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class AcServiceImpl implements AcService{
    private final AcRepository repository;
    private final ComRepository comRepository;

    public AcServiceImpl(AcRepository repository, ComRepository comRepository) {
        this.repository = repository;
        this.comRepository = comRepository;
    }

    @Override
    public List<Ac> findAll() {
        Specification<Ac> ac = AcSpecifications.deleted(false);
        return repository.findAll(ac);
    }

    @Override
    public Page<Ac> findAll(Specification<Ac> ac, Pageable pageable) {
        return repository.findAll(ac, pageable);
    }

    @Override
    public Page<Ac> findAllDeleted(Specification<Ac> ac, Pageable pageable) {
        ac = AcSpecifications.deleted(true);
        return repository.findAll(ac, pageable);
    }

    @Override
    public Page<Ac> findAllDeleted(Pageable pageable) {
        Specification<Ac> ac = AcSpecifications.deleted(true);
        return repository.findAll(ac, pageable);
    }

    @Override
    public Optional<Ac> findDeletedById(Integer id) {
        Specification<Ac> specification = AcSpecifications.id(id).and(AcSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Ac> findOne(Specification<Ac> specification) {

        return repository.findOne(specification);
    }

    @Override
    public Optional<Ac> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Ac save(Ac ac) {
        return repository.save(ac);
    }

    @Override
    public AcResponse create(AcRequest request) {

        Ac ac = AcMapper.INSTANCE.map(request);
        if (request.getComIds() != null && !request.getComIds().isEmpty()) {
            List<Com> coms = comRepository.findAllById(request.getComIds());

            if (coms.size() != request.getComIds().size()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,"One or more Com IDs were not found");
            }
            ac.setComs(coms);
        }
        repository.save(ac);
        return AcMapper.INSTANCE.map(ac);
    }

    @Override
    public Ac update(Integer id,AcRequest request) throws AcNotFound, ComNotFound, BadRequest {
        Ac ac= repository.findById(id).orElseThrow(() -> new AcNotFound("Ac not found: " + id));
        AcMapper.INSTANCE.updateAcFromRequest(request, ac);

        if (request.getComIds() != null && !request.getComIds().isEmpty()) {
            // Check for duplicate Com IDs
            var uniqueComIds = new HashSet<>(request.getComIds());
            if (uniqueComIds.size() != request.getComIds().size()) {
                throw new BadRequest("Duplicate Com IDs are not allowed");
            }
            var coms = comRepository.findAllById(request.getComIds());
            if (coms.size() != request.getComIds().size()) {
                throw new ComNotFound("One or more Com IDs were not found");
            }
            ac.setComs(coms);
        }

        return repository.save(ac);
    }

  //  @Override
    public Ac update1(Integer id,AcRequest request) {
        Ac ac= repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Ac not found: " + id));
        ac.setNom(request.getNom());
        ac.setAncrage(request.getAncrage());
        ac.setContact(request.getContact());
        ac.setNom(request.getNom());
        ac.setAncrage(request.getAncrage());
        ac.setContact(request.getContact());

        if (request.getComIds() != null && !request.getComIds().isEmpty()) {
            List<Com> coms = comRepository.findAllById(request.getComIds());

            if (coms.size() != request.getComIds().size()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,"One or more Com IDs were not found");
            }

            ac.setComs(coms);
        }

        return repository.save(ac);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }
}
