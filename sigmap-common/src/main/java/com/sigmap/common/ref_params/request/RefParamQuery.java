package com.sigmap.common.ref_params.request;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class RefParamQuery {
    private String search;

    private String code;

    private String description;

    private boolean deleted;

    public Specification<RefParam> toSpecification() {
        Specification<RefParam> spec = RefParamSpecifications.deleted(deleted);

        if (search !=null && !search.isBlank()) {
            spec = spec.and(RefParamSpecifications.search(search));
        }

        if (code != null && !code.isBlank()) {
            spec = spec.and(RefParamSpecifications.code(code));
        }

        if (description != null && !description.isBlank()) {
            spec = spec.and(RefParamSpecifications.description(description));
        }

        return spec;
    }
}
