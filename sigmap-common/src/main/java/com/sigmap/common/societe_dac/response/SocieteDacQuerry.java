package com.sigmap.common.societe_dac.response;

import com.sigmap.common.dac_prix.DacPrix;
import com.sigmap.common.dac_prix.DacPrixSpecifications;
import com.sigmap.common.entity.MethodePaiement;
import com.sigmap.common.societe_dac.SocieteDac;
import com.sigmap.common.societe_dac.SociteDacSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class SocieteDacQuerry {
    private Integer dacId;
    private Integer dacVer;
    private MethodePaiement methodePaiement;
    private Integer justifPaiementId;
    private Boolean statusPaiement;

    public Specification<SocieteDac> toSpecification() {
        Specification<SocieteDac> spec = Specification.where(null);

        if (dacId != null) {
            spec = spec.and(SociteDacSpecifications.withDacId(dacId));
        }

        if (dacVer != null) {
            spec = spec.and(SociteDacSpecifications.withDacver(dacVer));
        }

        if (methodePaiement != null) {
            spec = spec.and(SociteDacSpecifications.byMethodePaiement(methodePaiement));
        }

        if (justifPaiementId != null) {
            spec = spec.and(SociteDacSpecifications.justifPaiementId(justifPaiementId));
        }

        if (statusPaiement != null) {
            spec = spec.and(SociteDacSpecifications.byStatusPaiement(statusPaiement));
        }

        return spec;

    }
}
