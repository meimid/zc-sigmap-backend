package com.sigmap.common.dyna_form_fields;

import com.sigmap.common.dyna_form.DynaForm;
import com.sigmap.common.dyna_form_fields.request.DynaFormFieldRequest;

import java.util.List;

public interface DynaFormFieldService {
    List<DynaFormField> findByFormVersion(Integer formId, Integer formVer);

    /** copy a set of existing fields to a target version (same ids) */
    void copyFieldsToNewVersion(Integer formId, Integer newVer, List<DynaFormField> existing, DynaForm target);

    /** create new field rows for a target version (new ids via sequence) */
    void createNewFieldsForVersion(Integer formId, Integer newVer, List<DynaFormFieldRequest> adds, DynaForm target);

    /** compare existing vs incoming and return: unchanged (kept), additions, removals */
    Diff diff(Integer formId, Integer formVer, List<DynaFormFieldRequest> incoming);

    record Diff(List<DynaFormField> unchanged,
                List<DynaFormFieldRequest> additions,
                List<Integer> removedIds,
                boolean changed) {}
}
