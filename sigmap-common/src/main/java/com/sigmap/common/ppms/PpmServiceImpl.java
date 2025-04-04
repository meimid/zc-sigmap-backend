package com.sigmap.common.ppms;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ppms.request.PpmRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PpmServiceImpl implements PpmService {
    private final PpmRepository repository;
    //private final AcService acService;

    /**
     * Constructor.
     *
     * @param repository ppm repository
     *                    //TODO
//     * @param acService ac service
     */
    public PpmServiceImpl(PpmRepository repository) {
        this.repository = repository;
//        this.acService = acService;
    }

    @Override
    public List<Ppm> findAll() {
        Specification<Ppm> spec = PpmSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Ppm> findAll(Specification<Ppm> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Ppm> findAllDeleted(Specification<Ppm> spec, Pageable pageable) {
        Specification<Ppm> deletedSpec = PpmSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }

    @Override
    public Page<Ppm> findAllDeleted(Pageable pageable) {
        Specification<Ppm> spec = PpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Ppm> findDeletedById(Integer ppmId, Integer ppmVer) {
        Specification<Ppm> specification = PpmSpecifications.id(new CompositeId(ppmId, ppmVer))
                .and(PpmSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Ppm> findOne(Specification<Ppm> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Ppm> findById(Integer ppmId, Integer ppmVer) {
        return repository.findById(new CompositeId(ppmId, ppmVer));
    }

    @Override
    public Ppm save(Ppm ppm) {
        return repository.save(ppm);
    }

    @Override
    public Ppm create(PpmRequest request) {
        //TODO
        //Ac ac = acService.findById(request.getAcId()).orElseThrow(() -> new RuntimeException("Ac not found"));

        Ppm ppm = new Ppm();
        ppm.setId(request.getPpmId());
        ppm.setVersion(request.getPpmVer());
        ppm.setRef(request.getRef());
        ppm.setNom(request.getNom());
        ppm.setAnnee(request.getAnnee());
        //TODO: to verify later
        //ppm.setAc(ac);
        ppm.setBudgetEstime(request.getBudgetEstime());
        ppm.setStatusId(request.getStatusId());

        return save(ppm);
    }

    @Override
    public void delete(Integer ppmId, Integer ppmVer) {
        repository.softDelete(ppmId, ppmVer);
    }
}
