package com.sigmap.common.dyna_form;

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
import com.sigmap.common.entity.TypeDossier;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DynaFormServiceImpl implements DynaFormService {

    private final DynaFormRepository formRepo;
    private final DynaFormFieldRepository formFieldRepo;
    private final DynaFormFieldService fieldSvc;
    private final DynaFormMapper mapper;
    private final DynaFormFieldMapper fieldMapper;
    private final SequenceGeneratorService seq;
    private final EntityManager em;

    private static final String FORM_SEQ = "dyna_form_id_seq";
    private static final String FIELD_SEQ = "dyna_form_fields_id_seq";
    @Transactional
    @Override
    public DynaFormResponse create(DynaFormRequest req) {
        // new header
        DynaForm f = mapper.toHeaderEntity(req);
        f.setId(seq.getNextId(FORM_SEQ));
        f.setVersion(1);
        f.setStatus(ActiveStatus.ACTIF);
        formRepo.save(f);

        fieldSvc.createNewFieldsForVersion(f.getId(), f.getVersion(), req.getFields(), f);

        em.flush(); em.clear();
        DynaForm saved = formRepo.findById(new CompositeId(f.getId(), f.getVersion())).orElse(f);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public DynaFormResponse get(Integer id, Integer version) {
        DynaForm f = formRepo.findActiveByIdAndVersion(id, version)
                .orElseThrow(() -> new IllegalArgumentException("Form not found"));
        return mapper.toResponse(f);
    }

    @Transactional(readOnly = true)
    @Override
    public List<DynaFormResponse> list() {
        return formRepo.findAllActive(ActiveStatus.ACTIF).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public DynaFormResponse updateWithVersioning(Integer id, DynaFormRequest request) {
        Integer latestVer = formRepo.findMaxVersionById(id);
        if (latestVer == null) {
            return create(request);
        }

        DynaForm latest = formRepo.findActiveByIdAndVersion(id, latestVer)
                .orElseThrow(() -> new IllegalStateException("Form not found: id=" + id + ", ver=" + latestVer));

        List<DynaFormField> existing = fieldSvc.findByFormVersion(id, latestVer);
        Map<Integer, DynaFormField> existingById = existing.stream()
                .collect(Collectors.toMap(DynaFormField::getDynaFormFieldsId, f -> f));

        List<DynaFormFieldRequest> incoming = Optional.ofNullable(request.getFields()).orElseGet(List::of);

        Map<Integer, DynaFormFieldRequest> incomingById = incoming.stream()
                .filter(fr -> fr.getFieldId() != null)
                .collect(Collectors.toMap(DynaFormFieldRequest::getFieldId, fr -> fr, (a, b) -> b));

        Set<Integer> keepIds = new HashSet<>(incomingById.keySet());

        List<DynaFormFieldRequest> toAdd = incoming.stream()
                .filter(fr -> fr.getFieldId() == null)
                .collect(Collectors.toList());

        Set<Integer> toDropIds = new HashSet<>(existingById.keySet());
        toDropIds.removeAll(keepIds);

        boolean headerChanged =
                !Objects.equals(n(latest.getName()), n(request.getName())) ||
                        !Objects.equals(latest.getOrder(), request.getOrder()) ||
                        !Objects.equals(latest.getTypeDossier(), request.getTypeDossier());

        boolean keptEdits = false;
        for (Integer fid : keepIds) {
            DynaFormField oldF = existingById.get(fid);
            if (oldF == null) continue; // safety

            DynaFormFieldRequest rf = incomingById.get(fid);
            String newName        = rf.getName()        != null ? rf.getName()        : oldF.getName();
            String newLabel       = rf.getLabel()       != null ? rf.getLabel()       : oldF.getLabel();
            String newDesc        = rf.getDescription() != null ? rf.getDescription() : oldF.getDescription();
            Integer newTypeId     = rf.getTypeId()      != null ? rf.getTypeId()      : oldF.getTypeId();
            Boolean newRequired   = rf.getIsRequired()  != null ? rf.getIsRequired()  : oldF.getIsRequired();
            Integer newOrder      = rf.getOrder()       != null ? rf.getOrder()       : oldF.getOrder();

            if (!Objects.equals(n(oldF.getName()), n(newName)) ||
                    !Objects.equals(n(oldF.getLabel()), n(newLabel)) ||
                    !Objects.equals(n(oldF.getDescription()), n(newDesc)) ||
                    !Objects.equals(oldF.getTypeId(), newTypeId) ||
                    !Objects.equals(Boolean.TRUE.equals(oldF.getIsRequired()), Boolean.TRUE.equals(newRequired)) ||
                    !Objects.equals(oldF.getOrder(), newOrder)) {
                keptEdits = true;
                break;
            }
        }

        boolean fieldsChanged = keptEdits || !toAdd.isEmpty() || !toDropIds.isEmpty();

        if (!headerChanged && !fieldsChanged) {
            return mapper.toResponse(latest);
        }

        int newVer = latestVer + 1;

        DynaForm next = mapper.toHeaderEntity(request);
        next.setId(id);
        next.setVersion(newVer);
        next.setStatus(ActiveStatus.ACTIF);
        formRepo.save(next);

        int fallbackOrder = 0;

        List<Integer> orderedKeepIds = keepIds.stream()
                .map(existingById::get)
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(DynaFormField::getOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(DynaFormField::getDynaFormFieldsId))
                .map(DynaFormField::getDynaFormFieldsId)
                .toList();

        for (Integer fid : orderedKeepIds) {
            DynaFormField oldF = existingById.get(fid);
            DynaFormFieldRequest rf = incomingById.get(fid);

            DynaFormField nf = new DynaFormField();
            nf.setDynaFormId(id);
            nf.setDynaFormVer(newVer);
            nf.setDynaFormFieldsId(oldF.getDynaFormFieldsId());

            nf.setName(       rf.getName()        != null ? rf.getName()        : oldF.getName());
            nf.setLabel(      rf.getLabel()       != null ? rf.getLabel()       : oldF.getLabel());
            nf.setDescription(rf.getDescription() != null ? rf.getDescription() : oldF.getDescription());
            nf.setTypeId(     rf.getTypeId()      != null ? rf.getTypeId()      : oldF.getTypeId());
            nf.setIsRequired( rf.getIsRequired()  != null ? rf.getIsRequired()  : oldF.getIsRequired());

            Integer ord = (rf.getOrder() != null) ? rf.getOrder() : oldF.getOrder();
            nf.setOrder(ord != null ? ord : ++fallbackOrder);

            nf.setStatus(ActiveStatus.ACTIF);
            nf.setForm(next);

            formFieldRepo.save(nf);
            next.getFields().add(nf);
        }

        List<DynaFormFieldRequest> orderedAdds = toAdd.stream()
                .sorted(Comparator
                        .comparing(DynaFormFieldRequest::getOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(DynaFormFieldRequest::getName, Comparator.nullsLast(String::compareTo)))
                .toList();

        for (var fr : orderedAdds) {
            DynaFormField nf = fieldMapper.toEntity(fr);
            nf.setDynaFormId(id);
            nf.setDynaFormVer(newVer);
            nf.setDynaFormFieldsId(seq.getNextId(FIELD_SEQ)); // brand-new identity
            nf.setStatus(ActiveStatus.ACTIF);
            if (nf.getOrder() == null) nf.setOrder(++fallbackOrder);
            nf.setForm(next);

            formFieldRepo.save(nf);
            next.getFields().add(nf);
        }

        em.flush();
        em.clear();

        DynaForm saved = formRepo.findById(new CompositeId(id, newVer)).orElse(next);
        return mapper.toResponse(saved);
    }

    private static String n(String s) { return (s == null ? null : s.trim()); }


    @Transactional
    @Override
    public void softDelete(Integer id, Integer version) {
        DynaForm f = formRepo.findActiveByIdAndVersion(id, version)
                .orElseThrow(() -> new IllegalArgumentException("Form not found"));
        f.setIsDeleted(true);
        formRepo.save(f);
    }

    @Override
    public Page<DynaFormResponse> findByTypeDossier(TypeDossier type, Pageable pageable) {
        Page<DynaForm> page = formRepo.findByTypeDossier(type, pageable);
        return page.map(mapper::toResponse);
    }
}
