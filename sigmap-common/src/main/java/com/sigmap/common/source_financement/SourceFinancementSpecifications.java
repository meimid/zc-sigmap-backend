package com.sigmap.common.source_financement;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Specification for managing {@link SourceFinancement}. */
public class SourceFinancementSpecifications {

    private static String toLike(String value) {
        // Optional escaping to avoid wildcard injection in LIKE
        // If you don't want escaping, remove replace lines.
        String v = value.trim().toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + v + "%";
    }

    /** search across text fields. */
    public static Specification<SourceFinancement> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String like = toLike(search);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.like(cb.lower(root.get("code")), like, '\\'));
            predicates.add(cb.like(cb.lower(root.get("description")), like, '\\'));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    /** id equals. */
    public static Specification<SourceFinancement> id(Integer id) {
        return (root, query, cb) -> {
            if (id == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("id"), id);
        };
    }

    /** description like. */
    public static Specification<SourceFinancement> description(String description) {
        return (root, query, cb) -> {
            if (description == null || description.isBlank()) {
                return cb.conjunction();
            }
            String like = toLike(description);
            return cb.like(cb.lower(root.get("description")), like, '\\');
        };
    }

    /** deleted filter (soft delete). */
    public static Specification<SourceFinancement> deleted(boolean deleted) {
        return (root, query, cb) -> {
            if (!deleted) {
                return cb.or(
                        cb.isNull(root.get("isDeleted")),
                        cb.isFalse(root.get("isDeleted"))
                );
            }
            return cb.isTrue(root.get("isDeleted"));
        };
    }

    /** code like. */
    public static Specification<SourceFinancement> code(String code) {
        return (root, query, cb) -> {
            if (code == null || code.isBlank()) {
                return cb.conjunction();
            }
            String like = toLike(code);
            return cb.like(cb.lower(root.get("code")), like, '\\');
        };
    }

    /** bailleurDeFonds equals. */
    public static Specification<SourceFinancement> bailleurDeFonds(Boolean bailleurDeFonds) {
        return (Root<SourceFinancement> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (bailleurDeFonds == null) {
                return cb.conjunction(); // ✅ never return null
            }
            return cb.equal(root.get("bailleurDeFonds"), bailleurDeFonds);
        };
    }
}
