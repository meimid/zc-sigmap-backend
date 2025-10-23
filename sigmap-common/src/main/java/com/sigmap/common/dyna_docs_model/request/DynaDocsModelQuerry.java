package com.sigmap.common.dyna_docs_model.request;


import com.sigmap.common.dyna_docs_model.DynaDocsModel;
import com.sigmap.common.dyna_docs_model.DynaDocsModelSpecifications;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DynaDocsModelQuerry {

    private String search;
    private Integer dynaDocsModelId;
    private String description;

    public Specification<DynaDocsModel> toSpecification() {
        Specification<DynaDocsModel> spec = Specification.where(DynaDocsModelSpecifications.deleted(false));

        if (search != null && !search.isBlank()) {
            spec = spec.and(DynaDocsModelSpecifications.search(search));
        }
        if (dynaDocsModelId != null) {
            spec = spec.and(DynaDocsModelSpecifications.dynaDocsModelId(dynaDocsModelId));
        }
        if (description != null && !description.isBlank()) {
            spec = spec.and(DynaDocsModelSpecifications.description(description));
        }

        return spec;
    }
}
