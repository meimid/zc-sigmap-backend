package com.sigmap.common.dyna_form_fields;

import com.sigmap.common.dyna_form.DynaForm;
import com.sigmap.common.dyna_form.request.DynaFormRequest;
import com.sigmap.common.dyna_form_fields.DynaFormField;
import com.sigmap.common.dyna_form_fields.request.DynaFormFieldRequest;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DynaFormFieldServiceImpl implements DynaFormFieldService {

    private final DynaFormFieldRepository fieldRepo;
    private final SequenceGeneratorService seq;
    private final EntityManager em;

    private static final String FIELD_SEQ = "dyna_form_fields_id_seq";

    @Override
    public List<DynaFormField> findByFormVersion(Integer formId, Integer formVer) {
        return fieldRepo.findByFormVersion(formId, formVer);
    }

    @Override
    public Diff diff(Integer formId, Integer formVer, List<DynaFormFieldRequest> incomingRaw) {
        List<DynaFormField> existing = findByFormVersion(formId, formVer);
        Map<Integer, DynaFormField> existingById = existing.stream()
                .collect(Collectors.toMap(DynaFormField::getDynaFormFieldsId, f -> f));

        List<DynaFormFieldRequest> incoming = Optional.ofNullable(incomingRaw).orElseGet(List::of);
        Set<Integer> incomingIds = incoming.stream()
                .map(DynaFormFieldRequest::getDynaFormFieldsId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<DynaFormField> unchanged = new ArrayList<>();
        List<DynaFormFieldRequest> additions = new ArrayList<>();
        Set<Integer> removed = new HashSet<>(existingById.keySet());
        removed.removeAll(incomingIds); // present in DB but missing in request → removed

        for (var rf : incoming) {
            Integer fid = rf.getDynaFormFieldsId();
            if (fid == null) {
                additions.add(rf);
                continue;
            }
            var ex = existingById.get(fid);
            if (ex == null) {
                additions.add(rf);
                continue;
            }
            boolean same =
                    Objects.equals(n(ex.getName()), n(rf.getName())) &&
                            Objects.equals(n(ex.getLabel()), n(rf.getLabel())) &&
                            Objects.equals(n(ex.getDescription()), n(rf.getDescription())) &&
                            Objects.equals(ex.getType(), rf.getType()) &&
                            Objects.equals(Boolean.TRUE.equals(ex.getIsRequired()), Boolean.TRUE.equals(rf.getIsRequired())) &&
                            Objects.equals(ex.getOrder(), rf.getOrder());

            if (same) unchanged.add(ex);

        }

        boolean changed = !additions.isEmpty() || !removed.isEmpty();
        return new Diff(unchanged, additions, new ArrayList<>(removed), changed);
    }

    @Transactional
    @Override
    public void copyFieldsToNewVersion(Integer formId, Integer newVer, List<DynaFormField> existing, DynaForm target) {
        int fallbackOrder = 0;
        for (var oldF : existing) {
            DynaFormField nf = new DynaFormField();
            nf.setDynaFormId(formId);
            nf.setDynaFormVer(newVer);
            nf.setDynaFormFieldsId(oldF.getDynaFormFieldsId());
            nf.setName(oldF.getName());
            nf.setLabel(oldF.getLabel());
            nf.setDescription(oldF.getDescription());
            nf.setType(oldF.getType());
            nf.setIsRequired(oldF.getIsRequired());
            nf.setHasTotal(oldF.getHasTotal());
            nf.setIsFormula(oldF.getIsFormula());
            nf.setFormula(oldF.getFormula());
            nf.setOrder(oldF.getOrder() != null ? oldF.getOrder() : ++fallbackOrder);
            if(oldF.getStatus() != null)
            {
                nf.setStatus(oldF.getStatus());
            }
            else {
                nf.setStatus(ActiveStatus.ACTIF);
            }
            nf.setForm(target);

            fieldRepo.save(nf);
            target.getFields().add(nf);
        }
        em.flush();
    }


    @Transactional
    @Override
    public void createNewFieldsForVersion(Integer formId, Integer newVer,
                                          List<DynaFormFieldRequest> adds,
                                          DynaForm target) {
        int fallbackOrder = 0;
        List<DynaFormFieldRequest> sorted = new ArrayList<>(adds);
        sorted.sort(Comparator
                .comparing(DynaFormFieldRequest::getOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DynaFormFieldRequest::getName, Comparator.nullsLast(String::compareTo)));

        for (var rf : sorted) {
            DynaFormField nf = new DynaFormField();
            nf.setDynaFormId(formId);
            nf.setDynaFormVer(newVer);
            nf.setDynaFormFieldsId(seq.getNextId(FIELD_SEQ));
            nf.setName(rf.getName());
            nf.setLabel(rf.getLabel());
            nf.setDescription(rf.getDescription());
            nf.setType(rf.getType());
            nf.setIsRequired(Boolean.TRUE.equals(rf.getIsRequired()));
            nf.setHasTotal(Boolean.TRUE.equals(rf.getHasTotal()));
            nf.setIsFormula(Boolean.TRUE.equals(rf.getIsFormula()));
            nf.setFormula(rf.getFormula());
            nf.setOrder(rf.getOrder() != null ? rf.getOrder() : ++fallbackOrder);
            if(rf.getStatus() != null)
            {
            nf.setStatus(rf.getStatus());
            }
            else {
                nf.setStatus(ActiveStatus.ACTIF);
            }
            nf.setForm(target);

            fieldRepo.save(nf);
            target.getFields().add(nf);
        }
        em.flush();
    }

    private static String n(String s) { return s == null ? "" : s.trim(); }
}
