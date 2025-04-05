package com.sigmap.common.agpm;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.agpm.exceptions.AgpmAlreadyExistsException;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AgpmServiceImpl implements AgpmService {
    private final AgpmRepository repository;
    private final AcRepository acRepository;

    public AgpmServiceImpl(AgpmRepository repository, AcRepository acRepository) {
        this.repository = repository;
        this.acRepository = acRepository;
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
    public Page<Agpm> findAllDeleted(Specification<Agpm> spec, Pageable pageable) {
        Specification<Agpm> deletedSpec = AgpmSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }

    @Override
    public Page<Agpm> findAllDeleted(Pageable pageable) {
        Specification<Agpm> spec = AgpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Agpm> findDeletedById(Integer agpmId, Integer agpmVer) {
        Specification<Agpm> spec = AgpmSpecifications.id(new CompositeId(agpmId, agpmVer))
                .and(AgpmSpecifications.deleted(true));
        return repository.findOne(spec);
    }

    @Override
    public Optional<Agpm> findOne(Specification<Agpm> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Agpm> findById(Integer agpmId, Integer agpmVer) {
        return repository.findById(new CompositeId(agpmId, agpmVer));
    }

    @Override
    public Agpm save(Agpm agpm) {
        return repository.save(agpm);
    }

    @Override
    public Agpm create(AgpmRequest request) throws AgpmAlreadyExistsException {
        Integer id = request.getId();
        Integer version = request.getVersion();
        Optional<Agpm> existingAgpm = repository.findById(new CompositeId(id, version));

        if (existingAgpm.isPresent()) {
            throw new AgpmAlreadyExistsException(id, version, "Agpm with ID " + id + " and version " + version + " already exists.");
        }

        Agpm agpm = AgpmMapper.INSTANCE.toEntity(request);

        Integer acId = request.getAcId();
        System.out.println("Received acId: " + acId);
        if (acId != null) {
            Ac ac = acRepository.findById(acId)
                    .orElseThrow(() -> new IllegalArgumentException("Ac with ID " + acId + " not found"));
            agpm.setAc(ac);
            System.out.println("Set ac: " + agpm.getAc());
        }

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
        return savedAgpm;
    }

    @Override
    public void delete(Integer agpmId, Integer agpmVer) {
        repository.softDelete(agpmId, agpmVer);
    }

    @Override
    public Agpm update(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Optional<Agpm> existingAgpmOpt = repository.findById(compositeId);

        if (existingAgpmOpt.isEmpty()) {
            throw new AgpmNotFound(agpmId, agpmVer,
                    "Agpm with ID " + agpmId + " and version " + agpmVer + " was not found in the database.");
        }

        Agpm agpm = AgpmMapper.INSTANCE.updateAgpmFromRequest(request, existingAgpmOpt.get());

        Integer acId = request.getAcId();
        if (acId != null) {
            Ac ac = acRepository.findById(acId)
                    .orElseThrow(() -> new IllegalArgumentException("Ac with ID " + acId + " not found"));
            agpm.setAc(ac);
        }

        Jwt user = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        agpm.setUpdatedBy(user.getSubject());

        // Convert LocalDateTime to Calendar
        LocalDateTime now = LocalDateTime.now();
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        agpm.setUpdatedAt(calendar);

        return repository.save(agpm);
    }
}