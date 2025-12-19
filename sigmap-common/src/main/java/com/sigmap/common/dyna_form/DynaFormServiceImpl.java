package com.sigmap.common.dyna_form;

import com.sigmap.common.doc_soumission.exception.TypeDossierRequired;
import com.sigmap.common.dyna_form.mappers.DynaFormMapper;
import com.sigmap.common.dyna_form.request.DynaFormRequest;
import com.sigmap.common.dyna_form.response.DynaFormResponse;
import com.sigmap.common.dyna_form_fields.DynaFormField;
import com.sigmap.common.dyna_form_fields.DynaFormFieldRepository;
import com.sigmap.common.dyna_form_fields.DynaFormFieldService;
import com.sigmap.common.dyna_form_fields.mappers.DynaFormFieldMapper;
import com.sigmap.common.dyna_form_fields.request.DynaFormFieldRequest;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.type_dossier.TypeDossier;
import com.sigmap.common.type_dossier.TypeDossierService;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynaFormServiceImpl implements DynaFormService {

    private final DynaFormRepository formRepo;
    private final DynaFormFieldRepository formFieldRepo;
    private final DynaFormFieldService fieldSvc;
    private final DynaFormMapper mapper;
    private final DynaFormFieldMapper fieldMapper;
    private final SequenceGeneratorService seq;
    private final EntityManager em;
    private final TypeDossierService typeDossierService;

    private static final String FORM_SEQ = "dyna_form_id_seq";

    /** -------------------- CREATE -------------------- **/
    @Transactional
    @Override
    public DynaFormResponse create(DynaFormRequest req) {
        DynaForm f = mapper.toHeaderEntity(req);
        f.setId(seq.getNextId(FORM_SEQ));
        f.setVersion(1);
        f.setStatus(req.getStatus() != null ? req.getStatus() : ActiveStatus.ACTIF);

        if (req.getFormType() != null) {
            f.setFormType(req.getFormType());
        }
        if(req.getTypeDossierId() == null) {
            throw new TypeDossierRequired("Type dossier id is required");
        }
        TypeDossier typeDossier=typeDossierService.findById(req.getTypeDossierId());
        f.setTypeDossier(typeDossier);
        formRepo.save(f);
        fieldSvc.createNewFieldsForVersion(f.getId(), f.getVersion(), req.getFields(), f);

        em.flush();
        em.clear();

        DynaForm saved = formRepo.findById(new CompositeId(f.getId(), f.getVersion())).orElse(f);
        return mapper.toResponse(saved);
    }

    /** -------------------- GET -------------------- **/
    @Transactional(readOnly = true)
    @Override
    public DynaFormResponse get(Integer id, Integer version) {
        DynaForm f = formRepo.findActiveByIdAndVersion(id, version)
                .orElseThrow(() -> new IllegalArgumentException("Form not found"));
        return mapper.toResponse(f);
    }

    /** -------------------- GET LATEST VERSION -------------------- **/
    @Transactional(readOnly = true)
    @Override
    public DynaFormResponse getLatestVersion(Integer id) {
        Integer latestVer = formRepo.findMaxVersionById(id);
        if (latestVer == null) {
            throw new IllegalArgumentException("No versions found for DynaForm ID: " + id);
        }

        DynaForm latest = formRepo.findActiveByIdAndVersion(id, latestVer)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Latest version not found or deleted for DynaForm ID: " + id
                ));

        return mapper.toResponse(latest);
    }

    /** -------------------- LIST -------------------- **/
    @Transactional(readOnly = true)
    @Override
    public List<DynaFormResponse> list() {
        return formRepo.findAllActive(ActiveStatus.ACTIF).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /** -------------------- PAGINATED FIND ALL -------------------- **/
    @Transactional(readOnly = true)
    public Page<DynaFormResponse> findAll(Specification<DynaForm> spec, Pageable pageable) {
        Specification<DynaForm> base = Specification
                .where(DynaFormSpecifications.notDeleted())
                .and(DynaFormSpecifications.latestVersionPerId());

        Specification<DynaForm> combined = (spec != null) ? base.and(spec) : base;
        Page<DynaForm> page = formRepo.findAll(combined, pageable);

        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        return page.map(this::toFilteredResponse);
    }

    private DynaFormResponse toFilteredResponse(DynaForm form) {
        DynaFormResponse dto = mapper.toResponse(form);
        if (form.getFields() != null && !form.getFields().isEmpty()) {
            var kept = form.getFields().stream()
                    .filter(f -> Boolean.FALSE.equals(f.getIsDeleted()))
                    .map(fieldMapper::toResponse)
                    .toList();
            dto.setFields(kept);
        }
        return dto;
    }

    /** -------------------- UPDATE WITH VERSIONING -------------------- **/
    @Transactional
    @Override
    public DynaFormResponse updateWithVersioning(Integer id, DynaFormRequest request) {
        Integer latestVer = formRepo.findMaxVersionById(id);
        if (latestVer == null) {
            return create(request);
        }
        if(request.getTypeDossierId() == null) {
            throw new TypeDossierRequired("Type dossier id is required");
        }

        DynaForm latest = formRepo.findActiveByIdAndVersion(id, latestVer)
                .orElseThrow(() -> new IllegalStateException("Form not found: id=" + id + ", ver=" + latestVer));

        List<DynaFormField> existing = fieldSvc.findByFormVersion(id, latestVer);
        Map<Integer, DynaFormField> existingById = existing.stream()
                .collect(Collectors.toMap(DynaFormField::getDynaFormFieldsId, f -> f));

        List<DynaFormFieldRequest> incoming = Optional.ofNullable(request.getFields()).orElseGet(List::of);
        Map<Integer, DynaFormFieldRequest> incomingById = incoming.stream()
                .filter(fr -> fr.getDynaFormFieldsId() != null)
                .collect(Collectors.toMap(DynaFormFieldRequest::getDynaFormFieldsId, fr -> fr, (a, b) -> b));

        Set<Integer> keepIds = new HashSet<>(incomingById.keySet());
        List<DynaFormFieldRequest> toAdd = incoming.stream()
                .filter(fr -> fr.getDynaFormFieldsId() == null)
                .toList();

        Set<Integer> toDropIds = new HashSet<>(existingById.keySet());
        toDropIds.removeAll(keepIds);

        boolean headerChanged = mapper.headerChanged(latest, request);

        log.info(headerChanged ? "headerChanged" : "not headerChanged");

        boolean keptEdits = incoming.stream().anyMatch(rf -> {
            DynaFormField oldF = existingById.get(rf.getDynaFormFieldsId());
            if (oldF == null) return false;
            return !Objects.equals(oldF.getName(), rf.getName())
                    || !Objects.equals(oldF.getLabel(), rf.getLabel())
                    || !Objects.equals(oldF.getDescription(), rf.getDescription())
                    || !Objects.equals(oldF.getType(), rf.getType())
                    || !Objects.equals(oldF.getIsRequired(), rf.getIsRequired())
                    || !Objects.equals(oldF.getHasTotal(), rf.getHasTotal())
                    || !Objects.equals(oldF.getIsFormula(), rf.getIsFormula())
                    || !Objects.equals(oldF.getFormula(), rf.getFormula())
                    || !Objects.equals(oldF.getStatus(), rf.getStatus())
                    || !Objects.equals(oldF.getOrder(), rf.getOrder());
        });

        boolean fieldsChanged = keptEdits || !toAdd.isEmpty() || !toDropIds.isEmpty();

        log.info("fieldsChanged: " + fieldsChanged);
        log.info("keptEdits: " + keptEdits);
        log.info("toDropIds: " + toDropIds);
        log.info("toAdd: " + toAdd);

        if (!headerChanged && !fieldsChanged) {
            return mapper.toResponse(latest);
        }

        int newVer = latestVer + 1;
        DynaForm next = mapper.toHeaderEntity(request);
        next.setId(id);
        next.setVersion(newVer);
        next.setStatus(request.getStatus() != null ? request.getStatus() : latest.getStatus());
        next.setFormType(request.getFormType() != null ? request.getFormType() : latest.getFormType());
        TypeDossier typeDossier=typeDossierService.findById(request.getTypeDossierId());
        next.setTypeDossier(typeDossier);
        formRepo.save(next);

        int fallbackOrder = 0;

        for (Integer fid : keepIds) {
            DynaFormField oldF = existingById.get(fid);
            DynaFormFieldRequest rf = incomingById.get(fid);
            if (oldF == null) continue;

            DynaFormField nf = new DynaFormField();
            nf.setDynaFormId(id);
            nf.setDynaFormVer(newVer);
            nf.setDynaFormFieldsId(oldF.getDynaFormFieldsId());

            nf.setName(rf.getName() != null ? rf.getName() : oldF.getName());
            nf.setLabel(rf.getLabel() != null ? rf.getLabel() : oldF.getLabel());
            nf.setDescription(rf.getDescription() != null ? rf.getDescription() : oldF.getDescription());
            nf.setType(rf.getType() != null ? rf.getType() : oldF.getType());
            nf.setIsRequired(rf.getIsRequired() != null ? rf.getIsRequired() : oldF.getIsRequired());
            nf.setHasTotal(rf.getHasTotal() != null ? rf.getHasTotal() : oldF.getHasTotal());
            nf.setIsFormula(rf.getIsFormula() != null ? rf.getIsFormula() : oldF.getIsFormula());
            nf.setFormula(rf.getFormula() != null ? rf.getFormula() : oldF.getFormula());
            nf.setStatus(rf.getStatus() != null ? rf.getStatus() : oldF.getStatus());

            Integer ord = (rf.getOrder() != null) ? rf.getOrder() : oldF.getOrder();
            nf.setOrder(ord != null ? ord : ++fallbackOrder);
            nf.setForm(next);

            formFieldRepo.save(nf);
            next.getFields().add(nf);
        }

        fieldSvc.createNewFieldsForVersion(id, newVer, toAdd, next);

        em.flush();
        em.clear();

        DynaForm saved = formRepo.findById(new CompositeId(id, newVer)).orElse(next);
        return mapper.toResponse(saved);
    }

    private static String n(String s) { return (s == null ? null : s.trim()); }

    /** -------------------- SOFT DELETE -------------------- **/
    @Transactional
    @Override
    public void softDelete(Integer id, Integer version) {
        DynaForm f = formRepo.findActiveByIdAndVersion(id, version)
                .orElseThrow(() -> new IllegalArgumentException("Form not found"));
        f.setIsDeleted(true);
        formRepo.save(f);
    }

    /** -------------------- SOFT DELETE ALL VERSIONS -------------------- **/
    @Transactional
    @Override
    public void softDeleteAllVersions(Integer id) {
        List<DynaForm> versions = formRepo.findAllById(id);

        if (versions.isEmpty()) {
            throw new IllegalArgumentException("No forms found for ID: " + id);
        }

        versions.forEach(f -> {
            f.setIsDeleted(true);
            f.setDeletedAt(Calendar.getInstance());
        });

        formRepo.saveAll(versions);
    }

    /** -------------------- FIND BY TYPE -------------------- **/
    @Override
    public Page<DynaFormResponse> findByTypeDossier(Integer typeDossierId, Pageable pageable) {
        Page<DynaForm> page = formRepo.findByTypeDossier(typeDossierId, pageable);
        return page.map(mapper::toResponse);
    }
}
