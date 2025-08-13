package com.sigmap.common.delais_reglementaires;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DelaisReglementairesSpecifications {

    public static Specification<DelaisReglementaires> findById(Integer typeMarcherId, Integer modePassationId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

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

    public static Specification<DelaisReglementaires> typeMarcherId(Integer typeMarcherId) {
        return (root, query, criteriaBuilder) -> {
            if (typeMarcherId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("id").get("typeMarcher").get("id"), typeMarcherId);
        };
    }

    public static Specification<DelaisReglementaires> modePassationId(Integer modePassationId) {
        return (root, query, criteriaBuilder) -> {
            if (modePassationId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("id").get("modePassation").get("id"), modePassationId);
        };
    }

    public static Specification<DelaisReglementaires> delaiPublicationLancementMin(Integer delaiMin) {
        return (root, query, criteriaBuilder) -> {
            if (delaiMin == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("delaiPublicationLancement"), delaiMin);
        };
    }

    public static Specification<DelaisReglementaires> delaiPublicationLancementMax(Integer delaiMax) {
        return (root, query, criteriaBuilder) -> {
            if (delaiMax == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("delaiPublicationLancement"), delaiMax);
        };
    }

    public static Specification<DelaisReglementaires> delaiLancementAttributionMin(Integer delaiMin) {
        return (root, query, criteriaBuilder) -> {
            if (delaiMin == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("delaiLancementAttribution"), delaiMin);
        };
    }

    public static Specification<DelaisReglementaires> delaiLancementAttributionMax(Integer delaiMax) {
        return (root, query, criteriaBuilder) -> {
            if (delaiMax == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("delaiLancementAttribution"), delaiMax);
        };
    }

    public static Specification<DelaisReglementaires> delaiAttributionDemarrageMin(Integer delaiMin) {
        return (root, query, criteriaBuilder) -> {
            if (delaiMin == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("delaiAttributionDemarrage"), delaiMin);
        };
    }

    public static Specification<DelaisReglementaires> delaiAttributionDemarrageMax(Integer delaiMax) {
        return (root, query, criteriaBuilder) -> {
            if (delaiMax == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("delaiAttributionDemarrage"), delaiMax);
        };
    }
} 