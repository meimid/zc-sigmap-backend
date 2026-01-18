package com.sigmap.common.source_financement.request;

import com.sigmap.common.source_financement.SourceFinancement;
import com.sigmap.common.source_financement.SourceFinancementSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class SourceFinancementQuery {

    private String search;
    private Integer id;
    private String code;
    private String description;
    private Boolean bailleurDeFonds;

    private boolean deleted;

    public Specification<SourceFinancement> toSpecification() {
        Specification<SourceFinancement> spec =
                Specification.where(SourceFinancementSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(SourceFinancementSpecifications.search(search));
        }

        if (id != null) {
            spec = spec.and(SourceFinancementSpecifications.id(id));
        }

        if (code != null && !code.isBlank()) {
            spec = spec.and(SourceFinancementSpecifications.code(code));
        }

        if (description != null && !description.isBlank()) {
            spec = spec.and(SourceFinancementSpecifications.description(description));
        }

        if (bailleurDeFonds != null) {
            spec = spec.and(SourceFinancementSpecifications.bailleurDeFonds(bailleurDeFonds));
        }

        return spec;
    }
}
