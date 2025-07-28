package com.sigmap.common.ac_seuil;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AcSeuilSpecifications {


    public static Specification<AcSeuil> findById(Integer acId, Integer typeMarcherId, Integer modePassationId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by acId
            if (acId != null) {
                predicates.add(criteriaBuilder.equal(root.get("id").get("ac").get("id"), acId));
            }

            // Filter by typeMarcherId
            if (typeMarcherId != null) {
                predicates.add(criteriaBuilder.equal(root.get("id").get("typeMarcher").get("id"), typeMarcherId));
            }

            // Filter by modePassationId
            if (modePassationId != null) {
                predicates.add(criteriaBuilder.equal(root.get("id").get("modePassation").get("id"), modePassationId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<AcSeuil> acId(Integer acId) {
        return (root, query, criteriaBuilder) -> {
            if (acId == null) {
                return null; // Return null to ignore this filter if acId is null
            }
            return criteriaBuilder.equal(root.get("id").get("ac").get("id"), acId);
        };
    }

    public static Specification<AcSeuil> typeMarcherId(Integer typeMarcherId) {
        return (root, query, criteriaBuilder) -> {
            if (typeMarcherId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("id").get("typeMarcher").get("id"), typeMarcherId);
        };
    }

    public static Specification<AcSeuil> modePassationId(Integer modePassationId) {
        return (root, query, criteriaBuilder) -> {
            if (modePassationId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("id").get("modePassation").get("id"), modePassationId);
        };
    }

    public static Specification<AcSeuil> seuilMin(BigDecimal seuilMin) {
        return (root, query, criteriaBuilder) -> {
            if (seuilMin == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("seuil"), seuilMin);
        };
    }

    public static Specification<AcSeuil> seuilMax(BigDecimal seuilMax) {
        return (root, query, criteriaBuilder) -> {
            if (seuilMax == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("seuil"), seuilMax);
        };
    }

    public static Specification<AcSeuil> seuilExact(BigDecimal seuil) {
        return (root, query, criteriaBuilder) -> {
            if (seuil == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("seuil"), seuil);
        };
    }
}
