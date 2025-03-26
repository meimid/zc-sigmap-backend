package com.sigmap.common.agpm;

import com.sigmap.common.ac.AC;
import com.sigmap.common.agpm.request.AgpmRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sigmap.common.agpm.response.AgpmResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.print.Doc;

/**
 * Service implementation of {@link AgpmService}
 */
@Service
public class AgpmServiceImpl implements AgpmService {
    private final AgpmRepository repository;
//    private final ACService acService;
//    private final DocService docService;

    /**
     * Constructor.
     *
     * @param repository AGPM repository
     *                   //     * @param acService AC service
     *                   //     * @param docService Doc service
     */
    public AgpmServiceImpl(AgpmRepository repository /*ACService acService, DocService docService*/) {
        this.repository = repository;
//        this.acService = acService;
//        this.docService = docService;
    }

    @Override
    public List<Agpm> findAll() {
        Specification<Agpm> spec = AgpmSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Agpm> findAll(Specification<Agpm> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Agpm> findOne(Specification<Agpm> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Page<Agpm> findAllDeleted(Pageable pageable) {
        Specification<Agpm> spec = AgpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Agpm> findAllDeleted(Specification<Agpm> spec, Pageable pageable) {
        spec = AgpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Agpm> findDeletedById(Integer id) {
        Specification<Agpm> specification = AgpmSpecifications.id(id).and(AgpmSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Agpm> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Agpm save(Agpm agpm) {
        return repository.save(agpm);
    }

    @Override
     public Agpm create(AgpmRequest request) {
////        AC ac = acService.findById(request.getActId()).orElseThrow();
////
        Agpm agpm = new Agpm();
////
////        agpm.setRef(request.getRef());
////        agpm.setNom(request.getNom());
////        agpm.setContenu(request.getContenu());
////        agpm.setStatue(request.getStatus());
////        agpm.setAc(ac);
////
////        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
////            Set<Doc> docs = request.getDocIds().stream()
////                    .map(docId -> docService.findById(docId).orElseThrow())
////                    .collect(Collectors.toSet());
////            agpm.setDocs(docs);
////        }
////
        return save(agpm);
 }

@Override
    public Agpm update(Integer id, AgpmRequest request) {
        Agpm agpm = findById(id).orElseThrow();
////
////        agpm.setRef(request.getRef());
////        agpm.setNom(request.getNom());
////        agpm.setContenu(request.getContenu());
////        agpm.setStatue(request.getStatus());
////
////        AC ac = acService.findById(request.getActId()).orElseThrow();
////        agpm.setAc(ac);
////
////        if (request.getDocIds() != null) {
////            Set<Doc> docs = request.getDocIds().stream()
////                    .map(docId -> docService.findById(docId).orElseThrow())
////                    .collect(Collectors.toSet());
////            agpm.setDocs(docs);
////        }
////
        return save(agpm);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }
}