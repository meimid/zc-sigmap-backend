package com.sigmap.common.dyna_form.request;

import com.sigmap.common.dyna_form.DynaForm;
import com.sigmap.common.dyna_form.DynaFormSpecifications;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.FormType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
public class DynaFormQuery {

    private String search;
    private String typeDossier;
    private ActiveStatus status;
    private FormType formType;

    public Specification<DynaForm> toSpecification() {
        Specification<DynaForm> spec = Specification.where(null);

        if (status != null)
            spec = spec.and(DynaFormSpecifications.statusEq(status));

        if (typeDossier != null)
            spec = spec.and(DynaFormSpecifications.typeDossierEq(typeDossier));

        if (formType != null)
            spec = spec.and(DynaFormSpecifications.formTypeEq(formType));

        if (search != null && !search.isBlank())
            spec = spec.and(DynaFormSpecifications.search(search));

        return spec;
    }
}
