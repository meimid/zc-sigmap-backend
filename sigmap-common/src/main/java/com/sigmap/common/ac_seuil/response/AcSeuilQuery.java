package com.sigmap.common.ac_seuil.response;

import com.sigmap.common.ac_seuil.AcSeuil;
import com.sigmap.common.ac_seuil.AcSeuilSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
@Data
public class AcSeuilQuery {
    private Integer acId;
    private Integer typeMarcherId;
    private Integer modePassationId;
    private BigDecimal seuilMin;
    private BigDecimal seuilMax;
    private BigDecimal seuilExact;

    public Specification<AcSeuil> toSpecification() {
        Specification<AcSeuil> spec = Specification.where(null);

        if (acId != null) {
            spec = spec.and(AcSeuilSpecifications.acId(acId));
        }

        if (typeMarcherId != null) {
            spec = spec.and(AcSeuilSpecifications.typeMarcherId(typeMarcherId));
        }

        if (modePassationId != null) {
            spec = spec.and(AcSeuilSpecifications.modePassationId(modePassationId));
        }

        if (seuilMin != null) {
            spec = spec.and(AcSeuilSpecifications.seuilMin(seuilMin));
        }

        if (seuilMax != null) {
            spec = spec.and(AcSeuilSpecifications.seuilMax(seuilMax));
        }

        if (seuilExact != null) {
            spec = spec.and(AcSeuilSpecifications.seuilExact(seuilExact));
        }

        return spec;
    }
}
