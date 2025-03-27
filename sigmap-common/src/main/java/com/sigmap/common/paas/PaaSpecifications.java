package com.sigmap.common.paas;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ref_types.RefType;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class PaaSpecifications {
    /**
     * Search across ref, nom, and ac.nom fields.
     */
    public static Specification<Paa> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isEmpty()) {
                return criteriaBuilder.conjunction(); // Return all if search is null/empty
            }
            Join<Paa, Ac> join = root.join("ac");
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("ref"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("nom"), "%" + search + "%") // Assumes Ac has 'nom'
            );
        };
    }

    /**
     * Filter by composite ID (paaId and paaVer).
     */
    public static Specification<Paa> id(CompositeId id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) {
                return criteriaBuilder.conjunction(); // Return all if id is null
            }
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("paaId"), id.getId()),
                    criteriaBuilder.equal(root.get("paaVer"), id.getVersion())
            );
        };
    }

    /**
     * Filter by nom.
     */
    public static Specification<Paa> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            if (nom == null || nom.isEmpty()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.like(root.get("nom"), "%" + nom + "%");
        };
    }

    /**
     * Filter by ref.
     */
    public static Specification<Paa> ref(String ref) {
        return (root, query, criteriaBuilder) -> {
            if (ref == null || ref.isEmpty()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.like(root.get("ref"), "%" + ref + "%");
        };
    }

    /**
     * Filter by deletion status.
     */
    public static Specification<Paa> deleted(boolean deleted) {
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

    /**
     * Filter by annee.
     */
    public static Specification<Paa> annee(String annee) {
        return (root, query, criteriaBuilder) -> {
            if (annee == null || annee.isEmpty()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.equal(root.get("annee"), annee);
        };
    }

    /**
     * Filter by budgetEstime (range search).
     */
    public static Specification<Paa> budgetEstime(BigDecimal min, BigDecimal max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return criteriaBuilder.conjunction(); // Return all if both null
            }
            if (min != null && max != null) {
                return criteriaBuilder.between(root.get("budgetEstime"), min, max);
            } else if (min != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("budgetEstime"), min);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("budgetEstime"), max);
            }
        };
    }

    /**
     * Filter by statusId.
     */
    public static Specification<Paa> statusId(Integer statusId) {
        return (root, query, criteriaBuilder) -> {
            if (statusId == null) {
                return criteriaBuilder.conjunction(); // Return all if null
            }
            return criteriaBuilder.equal(root.get("statusId"), statusId);
        };
    }

    /**
     * Filter by ac.nom (Ac entity’s name).
     */
    public static Specification<Paa> acNom(String acNom) {
        return (root, query, criteriaBuilder) -> {
            if (acNom == null || acNom.isEmpty()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            Join<Paa, Ac> join = root.join("ac");
            return criteriaBuilder.like(join.get("nom"), "%" + acNom + "%");
        };
    }
}