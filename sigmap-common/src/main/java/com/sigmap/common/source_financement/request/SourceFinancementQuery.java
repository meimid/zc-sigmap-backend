package com.sigmap.common.source_financement.request;

import com.sigmap.common.source_financement.SourceFinancement;
import com.sigmap.common.source_financement.SourceFinancementSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

/**
 * Query parameters for filtering {@link SourceFinancement}.
 */
@Data
public class SourceFinancementQuery {
    private String search;
    private String code;
    private String description;
    private Boolean bailleurDeFonds;

    public Specification<SourceFinancement> toSpecification() {
        Specification<SourceFinancement> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            spec = spec.and(SourceFinancementSpecifications.search(search));
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
