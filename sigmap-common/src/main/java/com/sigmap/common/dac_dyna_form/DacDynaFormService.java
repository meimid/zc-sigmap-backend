package com.sigmap.common.dac_dyna_form;

import com.sigmap.common.dac_dyna_form.request.DacDynaFormRequest;
import com.sigmap.common.dac_dyna_form.response.DacDynaFormResponse;
import com.sigmap.common.dyna_form.response.DynaFormResponse;
import com.sigmap.common.entity.TypeDossier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DacDynaFormService {

    /**
     *  Attach a DynaForm (and all its fields) to a DAC.
     * Creates multiple records in dac_dyna_form, one per field.
     */
    List<DacDynaFormResponse> attachFormToDac(DacDynaFormRequest req);

    /**
     *  Update which fields of a DynaForm are displayed for a given DAC.
     * The field IDs in the list will have display=true, others false.
     */
    void updateFieldDisplayForDac(Integer dacId,
                                  Integer dacVer,
                                  Integer dynaFormId,
                                  Integer dynaFormVer,
                                  List<Integer> displayedFieldIds);

    /**
     *  Retrieve distinct attached forms (one row per form, not per field).
     * Used when displaying attached forms in DAC details.
     */
    Page<DynaFormResponse> findDistinctFormsByDac(Integer dacId,
                                                  Integer dacVer,
                                                  Pageable pageable);

    /**
     *  Retrieve all forms assigned to a specific DAC version (paginated).
     * (Raw DAC–Form–Field records, if you still need them.)
     */
    Page<DacDynaFormResponse> findByDac(Integer dacId,
                                        Integer dacVer,
                                        Pageable pageable);

    /**
     *  Filter DAC–Form associations by TypeDossier (Administrative / Financière / Technique).
     * Returns a paginated list.
     */
    Page<DacDynaFormResponse> findByTypeDossier(TypeDossier type, Pageable pageable);

    /**
     *  Retrieve forms that are NOT attached to the given DAC,
     * filtered by their TypeDossier (Administrative / Financière / Technique).
     */
    Page<DynaFormResponse> findUnattachedFormsByTypeDossier(Integer dacId,
                                                            Integer dacVer,
                                                            TypeDossier type,
                                                            Pageable pageable);

    /**
     *  Detach (delete) a specific DAC–Form–Field assignment.
     */
    void delete(DacDynaFormId id);
}
