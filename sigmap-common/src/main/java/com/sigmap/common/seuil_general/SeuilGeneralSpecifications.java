package com.sigmap.common.seuil_general;

import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class SeuilGeneralSpecifications {


    public static Specification<SeuilGeneral> search(String search) {
        return (root, query, criteriaBuilder) -> {

            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction(); // return all results
            }

            String likePattern = "%" + search + "%";

            // Join with typeMarcher (RefParam)
            Join<SeuilGeneral, RefParam> join = root.join("typeMarcher", JoinType.LEFT);

            return criteriaBuilder.or(

                    // Search montant (CAST as CHAR for MariaDB)
                    criteriaBuilder.like(
                            criteriaBuilder.function(
                                    "CAST",
                                    String.class,
                                    root.get("montant"),
                                    criteriaBuilder.literal("CHAR")
                            ),
                            likePattern
                    ),

                    // Search by RefParam.code
                    criteriaBuilder.like(join.get("code"), likePattern),

                    // Search by RefParam.description
                    criteriaBuilder.like(join.get("description"), likePattern),

                    // Search by RefParam.codeColor
                    criteriaBuilder.like(join.get("codeColor"), likePattern)
            );
        };
    }


    public static Specification<SeuilGeneral> typeMarcherId(Integer typeMarcherId) {
        return (root, query, criteriaBuilder) -> {
            if (typeMarcherId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("typeMarcher").get("id"), typeMarcherId);
        };
    }
    public static Specification<SeuilGeneral> deleted(boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (!deleted) {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("isDeleted")),
                        criteriaBuilder.isFalse(root.get("isDeleted")));
            }
            return criteriaBuilder.isTrue(root.get("isDeleted"));
        };
    }

    public static Specification<SeuilGeneral> montantMin(BigDecimal montantMin) {
        return (root, query, criteriaBuilder) -> {
            if (montantMin == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("montant"), montantMin);
        };
    }

    public static Specification<SeuilGeneral> montantMax(BigDecimal montantMax) {
        return (root, query, criteriaBuilder) -> {
            if (montantMax == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("montant"), montantMax);
        };
    }

    public static Specification<SeuilGeneral> montantExact(BigDecimal montant) {
        return (root, query, criteriaBuilder) -> {
            if (montant == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("montant"), montant);
        };
    }
}
