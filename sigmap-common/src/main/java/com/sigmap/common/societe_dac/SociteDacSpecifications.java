package com.sigmap.common.societe_dac;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac_prix.DacPrix;
import com.sigmap.common.entity.MethodePaiement;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class SociteDacSpecifications {

    public static Specification<SocieteDac> deleted(boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (!deleted) {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("isDeleted")),
                        criteriaBuilder.isFalse(root.get("isDeleted"))
                );
            }
            return criteriaBuilder.isTrue(root.get("isDeleted"));
        };
    }

    public static Specification<SocieteDac> withDacId(Integer dacId) {
        if (dacId == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id").get("dac").get("dacId"), dacId);
    }

    public static Specification<SocieteDac> withDacver(Integer dacVer) {
        if (dacVer == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id").get("dac").get("dacVer"), dacVer);
    }


    public static Specification<SocieteDac> byMethodePaiement(MethodePaiement methodePaiement) {
        return (root, query, criteriaBuilder) -> {
            if (methodePaiement == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("methodePaiement"), methodePaiement);
        };
    }
    public static Specification<SocieteDac> justifPaiementId(Integer JustifPaiementId) {
        return (root, query, criteriaBuilder) -> {
            if (JustifPaiementId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("justifPaiementId"), JustifPaiementId);
        };
    }

    public static Specification<SocieteDac> byStatusPaiement(boolean statusPaiement) {
        return (root, query, criteriaBuilder) -> {
            if (!statusPaiement) {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("statusPaiement")),
                        criteriaBuilder.isFalse(root.get("statusPaiement"))
                );
            }
            return criteriaBuilder.isTrue(root.get("statusPaiement"));
        };
    }



}
