package com.sigmap.common.dyna_form;

import com.sigmap.common.dyna_form.request.DynaFormRequest;
import com.sigmap.common.dyna_form.response.DynaFormResponse;
import com.sigmap.common.entity.TypeDossier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface DynaFormService {
    DynaFormResponse create(DynaFormRequest request);
    DynaFormResponse get(Integer id, Integer version);
    Page<DynaFormResponse> findAll(Specification<DynaForm> spec, Pageable pageable);
    List<DynaFormResponse> list();
    DynaFormResponse updateWithVersioning(Integer id, DynaFormRequest request);
    void softDelete(Integer id, Integer version);
    Page<DynaFormResponse> findByTypeDossier(TypeDossier type, Pageable pageable);
}
