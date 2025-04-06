package com.sigmap.common.agpm;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.agpm.exceptions.AgpmAlreadyExistsException;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for managing Agpm entities.
 * Handles CRUD operations and associations with Ac and Docs.
 */
@Service
public class AgpmServiceImpl implements AgpmService {
    private final AgpmRepository repository;
    private final AcRepository acRepository;
    private final DocsRepository docsRepository;

    /**
     * Constructor with dependency injection.
     *
     * @param repository     AgpmRepository for database operations
     * @param acRepository   AcRepository for Ac entity operations
     * @param docsRepository DocsRepository for Docs entity operations
     */
    public AgpmServiceImpl(AgpmRepository repository, AcRepository acRepository, DocsRepository docsRepository) {
        this.repository = repository;
        this.acRepository = acRepository;
        this.docsRepository = docsRepository;
    }

    /**
     * Utility method to map an Agpm entity to AgpmResponse, including actId and docIds.
     *
     * @param agpm The Agpm entity to map
     * @return AgpmResponse DTO
     */
    private AgpmResponse mapToResponse(Agpm agpm) {
        AgpmResponse response = AgpmMapper.INSTANCE.map(agpm);
        // Manually set fields not mapped by AgpmMapper
        response.setId(agpm.getId());
        response.setVersion(agpm.getVersion());
        response.setRef(agpm.getRef());
        response.setNom(agpm.getNom());
        response.setContenu(agpm.getContenu());
        response.setStatusId(agpm.getStatusId());
        response.setActId(agpm.getAc() != null ? agpm.getAc().getId() : null);
        response.setDocIds(agpm.getDocs() != null ? agpm.getDocs().stream().map(Docs::getId).collect(Collectors.toList()) : null);
        return response;
    }

    @Override
    public List<AgpmResponse> findAll() {
        Specification<Agpm> spec = AgpmSpecifications.deleted(false);
        List<Agpm> agpms = repository.findAll(spec);
        return agpms.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public Page<AgpmResponse> findAll(Specification<Agpm> spec, Pageable pageable) {
        Page<Agpm> agpmPage = repository.findAll(spec, pageable);
        return agpmPage.map(this::mapToResponse);
    }

    @Override
    public Page<AgpmResponse> findAllDeleted(Specification<Agpm> spec, Pageable pageable) {
        Specification<Agpm> deletedSpec = AgpmSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        Page<Agpm> agpmPage = repository.findAll(deletedSpec, pageable);
        return agpmPage.map(this::mapToResponse);
    }

    @Override
    public Page<AgpmResponse> findAllDeleted(Pageable pageable) {
        Specification<Agpm> spec = AgpmSpecifications.deleted(true);
        Page<Agpm> agpmPage = repository.findAll(spec, pageable);
        return agpmPage.map(this::mapToResponse);
    }

    @Override
    public Optional<AgpmResponse> findDeletedById(Integer agpmId, Integer agpmVer) {
        Specification<Agpm> spec = AgpmSpecifications.id(new CompositeId(agpmId, agpmVer))
                .and(AgpmSpecifications.deleted(true));
        return repository.findOne(spec).map(this::mapToResponse);
    }

    @Override
    public Optional<AgpmResponse> findOne(Specification<Agpm> specification) {
        return repository.findOne(specification).map(this::mapToResponse);
    }

    @Override
    public Optional<AgpmResponse> findById(Integer agpmId, Integer agpmVer) {
        return repository.findById(new CompositeId(agpmId, agpmVer)).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public Agpm save(Agpm agpm) {
        return repository.save(agpm);
    }

    /**
     * Creates a new Agpm entity from the given request.
     *
     * @param request The AgpmRequest containing the data to create the Agpm
     * @return The created Agpm entity as an AgpmResponse
     * @throws AgpmAlreadyExistsException if an Agpm with the same ID and version exists
     */
    @Override
    @Transactional
    public AgpmResponse create(AgpmRequest request) throws AgpmAlreadyExistsException {
        Integer id = request.getId();
        Integer version = request.getVersion();
        Optional<Agpm> existingAgpm = repository.findById(new CompositeId(id, version));

        if (existingAgpm.isPresent()) {
            throw new AgpmAlreadyExistsException(id, version, "Agpm with ID " + id + " and version " + version + " already exists.");
        }

        // Manually map AgpmRequest to Agpm since toEntity is removed from AgpmMapper
        Agpm agpm = new Agpm();
        agpm.setId(request.getId());
        agpm.setVersion(request.getVersion());
        agpm.setRef(request.getRef());
        agpm.setNom(request.getNom());
        agpm.setContenu(request.getContenu());
        agpm.setStatusId(request.getStatusId());

        // Set the Ac entity
        Integer acId = request.getAcId();
        System.out.println("Received acId: " + acId);
        if (acId != null) {
            Ac ac = acRepository.findById(acId)
                    .orElseThrow(() -> new IllegalArgumentException("Ac with ID " + acId + " not found"));
            agpm.setAc(ac);
            System.out.println("Set ac: " + agpm.getAc());
        }

        // Set the Docs entities (placeholder for Alfresco integration)
        List<Integer> docIds = request.getDocIds();
        if (docIds != null && !docIds.isEmpty()) {
            List<Docs> docs = docsRepository.findByIdIn(docIds);
            if (docs.size() != docIds.size()) {
                List<Integer> foundDocIds = docs.stream().map(Docs::getId).collect(Collectors.toList());
                List<Integer> missingDocIds = docIds.stream()
                        .filter(docId -> !foundDocIds.contains(docId))
                        .collect(Collectors.toList());
                if (!missingDocIds.isEmpty()) {
                    throw new IllegalArgumentException("Docs with IDs " + missingDocIds + " not found");
                }
            }
            agpm.setDocs(docs);
        }

        // Set metadata fields
        Jwt user = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String createdBy = user.getSubject();
        agpm.setCreatedBy(createdBy);

        // Convert LocalDateTime to Calendar
        LocalDateTime now = LocalDateTime.now();
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        agpm.setCreatedAt(calendar);

        agpm.setIsDeleted(false);

        System.out.println("Agpm before save: " + agpm);
        Agpm savedAgpm = repository.save(agpm);
        System.out.println("Saved Agpm: " + savedAgpm);

        // Map to AgpmResponse and manually set actId and docIds
        return mapToResponse(savedAgpm);
    }

    /**
     * Soft deletes an Agpm entity by ID and version.
     *
     * @param agpmId  The Agpm ID
     * @param agpmVer The Agpm version
     * @throws AgpmNotFound if the Agpm entity is not found
     */
    @Override
    @Transactional
    public void delete(Integer agpmId, Integer agpmVer) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);

        // Check if the Agpm entity exists
        Optional<Agpm> existingAgpm = repository.findById(compositeId);
        if (existingAgpm.isEmpty()) {
            throw new AgpmNotFound(agpmId, agpmVer,
                    "Agpm with ID " + agpmId + " and version " + agpmVer + " was not found in the database.");
        }

        // Perform the soft delete using SoftDeleteRepository's method
        Integer rowsAffected = repository.softDelete(agpmId,agpmVer);
        if (rowsAffected != 1) {
            throw new IllegalStateException("Soft delete failed: Expected 1 row to be affected, but " + rowsAffected + " rows were affected for Agpm with ID " + agpmId + " and version " + agpmVer);
        }
    }

    /**
     * Updates an existing Agpm entity.
     *
     * @param agpmId  The Agpm ID
     * @param agpmVer The Agpm version
     * @param request The AgpmRequest containing the updated data
     * @return The updated Agpm entity as an AgpmResponse
     * @throws AgpmNotFound if the Agpm is not found
     */
    @Override
    @Transactional
    public AgpmResponse update(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Optional<Agpm> existingAgpmOpt = repository.findById(compositeId);

        if (existingAgpmOpt.isEmpty()) {
            throw new AgpmNotFound(agpmId, agpmVer,
                    "Agpm with ID " + agpmId + " and version " + agpmVer + " was not found in the database.");
        }

        // Manually update Agpm from AgpmRequest since updateAgpmFromRequest is removed from AgpmMapper
        Agpm agpm = existingAgpmOpt.get();
        agpm.setRef(request.getRef());
        agpm.setNom(request.getNom());
        agpm.setContenu(request.getContenu());
        agpm.setStatusId(request.getStatusId());

        Integer acId = request.getAcId();
        if (acId != null) {
            Ac ac = acRepository.findById(acId)
                    .orElseThrow(() -> new IllegalArgumentException("Ac with ID " + acId + " not found"));
            agpm.setAc(ac);
        }

        // Update the Docs entities (placeholder for Alfresco integration)
        List<Integer> docIds = request.getDocIds();
        if (docIds != null) {
            List<Docs> docs = docIds.isEmpty() ? List.of() : docsRepository.findByIdIn(docIds);
            if (!docIds.isEmpty() && docs.size() != docIds.size()) {
                List<Integer> foundDocIds = docs.stream().map(Docs::getId).collect(Collectors.toList());
                List<Integer> missingDocIds = docIds.stream()
                        .filter(docId -> !foundDocIds.contains(docId))
                        .collect(Collectors.toList());
                if (!missingDocIds.isEmpty()) {
                    throw new IllegalArgumentException("Docs with IDs " + missingDocIds + " not found");
                }
            }
            agpm.setDocs(docs);
        }

        Jwt user = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        agpm.setUpdatedBy(user.getSubject());

        // Convert LocalDateTime to Calendar
        LocalDateTime now = LocalDateTime.now();
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        agpm.setUpdatedAt(calendar);

        Agpm updatedAgpm = repository.save(agpm);

        // Map to AgpmResponse and manually set actId and docIds
        return mapToResponse(updatedAgpm);
    }
}