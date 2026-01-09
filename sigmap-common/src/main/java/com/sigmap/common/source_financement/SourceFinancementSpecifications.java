package com.sigmap.common.source_financement;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Specification for managing {@link SourceFinancement}. */
public class SourceFinancementSpecifications {
    /** search. */
    public static Specification<SourceFinancement> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            String like = "%" + search.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // text fields
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), like));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), like));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    /** id. */
    public static Specification<SourceFinancement> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** description. */
    public static Specification<SourceFinancement> description(String description) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("description"), "%" + description + "%");
        };
    }

    /** deleted. */
    public static Specification<SourceFinancement> deleted(boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (!deleted) {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("isDeleted")),
                        criteriaBuilder.isFalse(root.get("isDeleted")));
            }
            return criteriaBuilder.isTrue(root.get("isDeleted"));
        };
    }

    /** code. */
    public static Specification<SourceFinancement> code(String code) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("code"), "%" + code + "%");
        };
    }

    /** bailleurDeFonds. */
    public static Specification<SourceFinancement> bailleurDeFonds(Boolean bailleurDeFonds) {
        return (Root<SourceFinancement> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (bailleurDeFonds == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("bailleurDeFonds"), bailleurDeFonds);
        };
    }
}
