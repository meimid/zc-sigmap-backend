package com.sigmap.common.dac_prix.response;

import com.sigmap.common.dac_prix.DacPrix;
import com.sigmap.common.dac_prix.DacPrixId;
import com.sigmap.common.dac_prix.DacPrixSpecifications;
import com.sigmap.common.dyna_docs.DynaDocs;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

@Data
public class DacPrixQuery {
    private Integer dacId;
    private Integer dacVer;
    private Integer monnaieId;
    private BigDecimal prix;


    public Specification<DacPrix> toSpecification() {
        Specification<DacPrix> spec = Specification.where(null);

        if (dacId != null) {
            spec = spec.and(DacPrixSpecifications.withDacId(dacId));
        }

        if (dacVer != null) {
            spec = spec.and(DacPrixSpecifications.withDacver(dacVer));
        }

        if (monnaieId != null) {
            spec = spec.and(DacPrixSpecifications.withMonnaie(monnaieId));
        }

        if (prix != null) {
            spec = spec.and(DacPrixSpecifications.withPrix(prix));
        }

        return spec;

    }

}
