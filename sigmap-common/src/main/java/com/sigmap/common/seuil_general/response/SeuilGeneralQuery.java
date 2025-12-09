package com.sigmap.common.seuil_general.response;

import com.sigmap.common.seuil_general.SeuilGeneral;
import com.sigmap.common.seuil_general.SeuilGeneralSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

@Data
public class SeuilGeneralQuery {
    private Integer typeMarcherId;
    private boolean deleted;
    private BigDecimal montantMin;
    private BigDecimal montantMax;
    private BigDecimal montantExact;

    public Specification<SeuilGeneral> toSpecification() {
        Specification<SeuilGeneral> spec = Specification.where(SeuilGeneralSpecifications.deleted(deleted));

        if (typeMarcherId != null) {
            spec = spec.and(SeuilGeneralSpecifications.typeMarcherId(typeMarcherId));
        }

        if (montantMin != null) {
            spec = spec.and(SeuilGeneralSpecifications.montantMin(montantMin));
        }

        if (montantMax != null) {
            spec = spec.and(SeuilGeneralSpecifications.montantMax(montantMax));
        }

        if (montantExact != null) {
            spec = spec.and(SeuilGeneralSpecifications.montantExact(montantExact));
        }

        return spec;
    }
}
