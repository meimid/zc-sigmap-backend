package com.sigmap.common.dac_prix;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;


public class DacPrixSpecifications {


    public static Specification<DacPrix> withDacId(Integer dacId) {
        if (dacId == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("id").get("dac").get("dacId"), dacId);
    }

    public static Specification<DacPrix> withDacver(Integer dacVer) {
        if (dacVer == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id").get("dac").get("dacVer"), dacVer);
    }


    public static Specification<DacPrix> withMonnaie(Integer monnaieId) {
        if (monnaieId == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id").get("monnaie").get("id"), monnaieId);
    }




    public static Specification<DacPrix> withPrix(BigDecimal prix) {
        return (root, query, criteriaBuilder) -> {
            if (prix == null ) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("prix"), prix);
        };
    }
}
