package com.sigmap.common.dac_dyna_form.request;

import com.sigmap.common.dac_dyna_form.DacDynaForm;
import com.sigmap.common.dac_dyna_form.DacDynaFormSpecifications;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
public class DacDynaFormQuery {

    private String search;
    private Integer dacId;
    private Integer dacVer;
    private Integer dynaFormId;
    private String typeDossier;
    private Boolean display;

    /**
     * Build a JPA Specification dynamically from query parameters.
     */
    public Specification<DacDynaForm> toSpecification() {
        Specification<DacDynaForm> spec = Specification.where(null);

        if (dacId != null)
            spec = spec.and(DacDynaFormSpecifications.byDacId(dacId));

        if (dacVer != null)
            spec = spec.and(DacDynaFormSpecifications.byDacVer(dacVer));

        if (dynaFormId != null)
            spec = spec.and(DacDynaFormSpecifications.byDynaFormId(dynaFormId));

        if (typeDossier != null)
            spec = spec.and(DacDynaFormSpecifications.byTypeDossierCode(typeDossier));

        if (display != null)
            spec = spec.and(DacDynaFormSpecifications.byDisplay(display));

        if (search != null && !search.isBlank())
            spec = spec.and(DacDynaFormSpecifications.search(search));

        return spec;
    }
}
