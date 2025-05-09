package com.sigmap.common.agpm;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.*;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


/**
 * Service implementation for managing Agpm entities.
 * Handles CRUD operations and associations with Ac and Docs.
 */
@Service
@Slf4j
public class AgpmServiceImpl implements AgpmService {
    private final AgpmRepository repository;
    private final AcRepository acRepository;
    private final DocsRepository docsRepository;
    private final AgpmMapper agpmMapper;
    private final RefParamRepository refParamRepository;
    private final MessageSource messageSource;

    public AgpmServiceImpl(AgpmRepository repository, AcRepository acRepository, DocsRepository docsRepository, AgpmMapper agpmMapper, RefParamRepository refParamRepository, MessageSource messageSource) {
        this.repository = repository;
        this.acRepository = acRepository;
        this.docsRepository = docsRepository;
        this.agpmMapper = agpmMapper;
        this.refParamRepository = refParamRepository;
        this.messageSource = messageSource;
    }

    private Locale getLocal (){
        return LocaleContextHolder.getLocale();
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
    public Page<Agpm> findAllDeleted(Pageable pageable) {
        Specification<Agpm> spec = AgpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }


    @Override
    public Optional<Agpm> findOne(Specification<Agpm> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Agpm findById(Integer agpmId, Integer agpmVer) throws AgpmNotFound {
        return repository.findById(new CompositeId(agpmId, agpmVer))
                .orElseThrow(() -> new AgpmNotFound("", List.of(agpmId), List.of(agpmVer)));
    }

    @Override
    public Agpm save(Agpm agpm) {
        return repository.save(agpm);
    }


    @Override
    public Agpm create(AgpmRequest request) throws AgpmAlreadyExistsException, AgpmNotNull, AcNotFound, DocNotFound, AgpmDefaultStatus {
        Integer version = request.getVersion();
        if (version == null) {
            throw new AgpmNotNull("");
        }

        Integer id = request.getId();
        if (id == null) {
            id = repository.findMaxId();
            if (id == null) {
                id = 1;
            } else {
                id++;
            }
        }

        Optional<Agpm> existingAgpm = repository.findById(new CompositeId(id, version));
        if (existingAgpm.isPresent()) {
            throw new AgpmAlreadyExistsException("",id, version);
        }

        Agpm agpm = agpmMapper.toEntity(request);
        agpm.setId(id);
        agpm.setVersion(version);

        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new AcNotFound(" " , List.of(request.getAcId())));
            agpm.setAc(ac);
        }

        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = docsRepository.findByIdIn(request.getDocIds());
            if (docs.size() != request.getDocIds().size()) {
                List<Integer> foundDocIds = docs.stream().map(Docs::getId).toList();
                List<Integer> missingDocIds = request.getDocIds().stream()
                        .filter(docId -> !foundDocIds.contains(docId)).toList();
                throw new DocNotFound("", missingDocIds);
            }
            agpm.setDocs(docs);
        } else if (request.getDocIds() != null) {
            agpm.setDocs(List.of());
        }

        agpm.setIsDeleted(false);

        RefParam defaultStatus = refParamRepository.findById(1)
                .orElseThrow(() -> new AgpmDefaultStatus(""));
        agpm.setStatus(defaultStatus);

        return repository.save(agpm);
    }


    @Override
    public void delete(Integer agpmId, Integer agpmVer) throws AgpmNotFound, AgpmSoftDelete {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Optional<Agpm> existingAgpm = repository.findById(compositeId);
        if (existingAgpm.isEmpty()) {
            throw new AgpmNotFound("",List.of(agpmId),List.of(agpmVer));
        }

        Integer rowsAffected = repository.softDelete(agpmId, agpmVer, Calendar.getInstance());
        if (rowsAffected != 1) {
            throw new AgpmSoftDelete("" , rowsAffected ,  agpmId , agpmVer);
        }
    }

    @Override
    @Transactional
    public Agpm update(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound, AcNotFound, DocNotFound, ReferenceNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm agpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound("", List.of(agpmId), List.of(agpmId)));

        if (Boolean.TRUE.equals(agpm.getIsDeleted())) {
            throw new IllegalArgumentException("Cannot update Agpm with ID " + agpmId + " and version " + agpmVer + " because it is marked as deleted.");
        }

        agpmMapper.updateAgpmFromRequest(request, agpm);

        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new AcNotFound("" , List.of(request.getAcId() )));
            agpm.setAc(ac);
        } else {
            agpm.setAc(null);
        }

        // Update DOCS
        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = docsRepository.findByIdIn(request.getDocIds());
            if (docs.size() != request.getDocIds().size()) {
                List<Integer> foundDocIds = docs.stream().map(Docs::getId).toList();
                List<Integer> missingDocIds = request.getDocIds().stream()
                        .filter(docId -> !foundDocIds.contains(docId))
                        .toList();
                throw new DocNotFound(" " , missingDocIds);
            }
            agpm.setDocs(docs);
        } else if (request.getDocIds() != null) {
            agpm.setDocs(List.of());
        }

        List<String> notFoundRefs = new ArrayList<>();

        if (request.getStatusId() != null) {
            RefParam status = refParamRepository.findById(request.getStatusId()).get();
            agpm.setStatus(status);
        } else {
            agpm.setStatus(agpm.getStatus());
            notFoundRefs.add(messageSource.getMessage("ppm.status", new Integer[]{request.getStatusId()}, getLocal()));
        }


        if (!notFoundRefs.isEmpty()) {
        throw new ReferenceNotFound(" " , List.of(String.join(", ", notFoundRefs)));
    }

        return repository.save(agpm);
    }



    @Override
    @Transactional
    public Agpm updateStatus(Integer agpmId, Integer agpmVer, Integer statusId) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm agpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound("", List.of(agpmId), List.of( agpmVer)));

        List<String> notFoundRefs = new ArrayList<>();

        RefParam newStatus = refParamRepository.findById(statusId).get();

        if (newStatus != null) {
            agpm.setStatus(newStatus);
        } else {
            notFoundRefs.add(messageSource.getMessage("ppm.status", new Integer[]{statusId}, getLocal()));
        }


        if (!notFoundRefs.isEmpty()) {
            throw new ReferenceNotFound(" " , List.of(String.join(", ", notFoundRefs)));
        }

        agpm.setUpdatedAt(Calendar.getInstance());
        agpm.setUpdatedBy("system");

        return repository.save(agpm);
    }

}