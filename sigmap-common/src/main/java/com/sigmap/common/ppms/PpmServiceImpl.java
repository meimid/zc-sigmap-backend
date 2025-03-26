package com.sigmap.common.ppms;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ppms.request.PpmRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

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
        spec = PpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Ppm> findAllDeleted(Pageable pageable) {
        Specification<Ppm> spec = PpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Ppm> findDeletedById(Integer id) {
        Specification<Ppm> specification = PpmSpecifications.id(id).and(PpmSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Ppm> findOne(Specification<Ppm> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Ppm> findById(Integer id) {
        return repository.findById(id); // Custom method, see notes
    }

    @Override
    public Ppm save(Ppm ppm) {
        return repository.save(ppm);
    }

    @Override
    public Ppm create(PpmRequest request) {
        //TODO
        //Ac ac = acService.findById(request.getAcId()).orElseThrow();

        Ppm ppm = new Ppm();

        ppm.setPpmId(request.getPpmId());
        ppm.setPpmVer(request.getPpmVer());
        ppm.setRef(request.getRef());
        ppm.setNom(request.getNom());
        ppm.setAnnee(request.getAnnee());
        //TODO:to verify later
//        ppm.setAc(ac);
        ppm.setBudgetEstime(request.getBudgetEstime());
        ppm.setStatusId(request.getStatusId());

        return save(ppm);
    }

    @Override
    public void delete(Integer id) {
        // Note: Assumes id is ppmId; adjust for composite key if needed
        repository.softDelete(id); // Custom method, see notes
    }
}
