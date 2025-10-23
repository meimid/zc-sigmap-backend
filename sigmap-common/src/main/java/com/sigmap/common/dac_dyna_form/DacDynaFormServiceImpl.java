package com.sigmap.common.dac_dyna_form;

import com.sigmap.common.dac_dyna_form.exceptions.DacDynaFormNotFoundException;
import com.sigmap.common.dac_dyna_form.mappers.DacDynaFormMapper;
import com.sigmap.common.dac_dyna_form.request.DacDynaFormRequest;
import com.sigmap.common.dac_dyna_form.response.DacDynaFormResponse;
import com.sigmap.common.dyna_form.DynaForm;
import com.sigmap.common.dyna_form.DynaFormRepository;
import com.sigmap.common.dyna_form.mappers.DynaFormMapper;
import com.sigmap.common.dyna_form.response.DynaFormResponse;
import com.sigmap.common.dyna_form_fields.DynaFormFieldRepository;
import com.sigmap.common.entity.TypeDossier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DacDynaFormServiceImpl implements DacDynaFormService {

    private final DacDynaFormRepository repo;
    private final DynaFormRepository formRepo;
    private final DynaFormFieldRepository fieldRepo;
    private final DacDynaFormMapper mapper;
    private final DynaFormMapper DynaFormMapper;

    // ------------------------------------------------------------
    // Attach a full DynaForm to a DAC
    // ------------------------------------------------------------
    @Transactional
    @Override
    public List<DacDynaFormResponse> attachFormToDac(DacDynaFormRequest req) {
        List<Integer> fieldIds = fieldRepo.findIdsByForm(req.getDynaFormId(), req.getDynaFormVer());
        if (fieldIds.isEmpty()) {
            throw new DacDynaFormNotFoundException("No fields found for the provided DynaForm.");
        }

        List<DacDynaFormResponse> responses = new ArrayList<>();

        for (Integer fieldId : fieldIds) {
            DacDynaForm entity = new DacDynaForm();
            entity.setDacId(req.getDacId());
            entity.setDacVer(req.getDacVer());
            entity.setDynaFormId(req.getDynaFormId());
            entity.setDynaFormVer(req.getDynaFormVer());
            entity.setDynaFormFieldsId(fieldId);
            entity.setDisplay(true);
            entity.setIsDeleted(false);
            repo.save(entity);
            responses.add(mapper.toResponse(entity));
        }
        return responses;
    }

    // ------------------------------------------------------------
    // Update which fields are displayed (true/false)
    // ------------------------------------------------------------
    @Transactional
    @Override
    public void updateFieldDisplayForDac(Integer dacId, Integer dacVer,
                                         Integer dynaFormId, Integer dynaFormVer,
                                         List<Integer> displayedFieldIds) {
        List<DacDynaForm> records = repo.findByDacAndForm(dacId, dacVer, dynaFormId, dynaFormVer)
                .stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .collect(Collectors.toList());

        if (records.isEmpty()) {
            throw new DacDynaFormNotFoundException("No active DAC–Form records found to update display settings.");
        }

        for (DacDynaForm record : records) {
            boolean shouldDisplay = displayedFieldIds != null &&
                    displayedFieldIds.contains(record.getDynaFormFieldsId());
            record.setDisplay(shouldDisplay);
            repo.save(record);
        }
    }

    // ------------------------------------------------------------
    // Retrieve distinct attached forms (one per form, not per field)
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    @Override
    public Page<DynaFormResponse> findDistinctFormsByDac(Integer dacId, Integer dacVer, Pageable pageable) {
        List<Object[]> distinct = repo.findDistinctFormsByDac(dacId, dacVer);

        List<DynaForm> forms = distinct.stream()
                .map(arr -> formRepo.findById(new com.sigmap.common.entity.CompositeId(
                        (Integer) arr[0], (Integer) arr[1]
                )).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        List<DynaFormResponse> responses = forms.stream()
                .filter(f -> f.getIsDeleted() == null || !f.getIsDeleted()) // exclude deleted
                .map(DynaFormMapper::toResponse)
                .toList();

        return new PageImpl<>(responses, pageable, responses.size());
    }

    // ------------------------------------------------------------
    // Retrieve unattached forms filtered by TypeDossier
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    @Override
    public List<DynaFormResponse> findUnattachedFormsByTypeDossier(Integer dacId,
                                                                   Integer dacVer,
                                                                   TypeDossier type,
                                                                   Pageable pageable) {
        List<DynaForm> unattachedForms = formRepo.findAll(
                DacDynaFormSpecifications.unattachedToDac(dacId, dacVer, type)
        );

        List<DynaFormResponse> responses = unattachedForms
                .stream()
                .filter(f -> f.getIsDeleted() == null || !f.getIsDeleted())
                .map(mapper::toDynaFormResponse)
                .collect(Collectors.toList());

        return responses;
    }

    // ------------------------------------------------------------
    // Soft Delete (detach) all field rows of a DAC–Form relation
    // ------------------------------------------------------------
    @Transactional
    @Override
    public void delete(DacDynaFormId id) {
        int updated = repo.softDelete(
                id.getDacId(),
                id.getDacVer(),
                id.getDynaFormId(),
                id.getDynaFormVer(),
                Calendar.getInstance()
        );

        if (updated == 0) {
            throw new DacDynaFormNotFoundException("No records found or already deleted for this DAC–Form relation.");
        }
    }

    // ------------------------------------------------------------
    // Retrieve all forms assigned to a specific DAC version (paginated)
    // ------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public Page<DacDynaFormResponse> findByDac(Integer dacId, Integer dacVer, Pageable pageable) {
        Page<DacDynaForm> page = repo.findByDacIdAndDacVer(dacId, dacVer, pageable);
        List<DacDynaFormResponse> filtered = page.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    // ------------------------------------------------------------
    // Filter DAC–Form associations by TypeDossier (paginated)
    // ------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public Page<DacDynaFormResponse> findByTypeDossier(TypeDossier type, Pageable pageable) {
        Page<DacDynaForm> page = repo.findByTypeDossier(type, pageable);
        List<DacDynaFormResponse> filtered = page.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(filtered, pageable, filtered.size());
    }
}
