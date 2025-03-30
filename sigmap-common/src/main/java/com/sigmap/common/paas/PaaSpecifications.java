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
            if (search == null || search.isBlank()) {
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
     * Filter by paa_id.
     */
    public static Specification<Paa> id(CompositeId id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("paaId"), id.getId()),
                    criteriaBuilder.equal(root.get("paaVer"), id.getVersion())
            );
        };
    }

    /**
     * Filter by paa_ver.
     */
    public static Specification<Paa> version(Integer version) {
        return (root, query, criteriaBuilder) -> {
            if (version == null) {
                return criteriaBuilder.conjunction(); // Return all if null
            }
            return criteriaBuilder.equal(root.get("version"), version);
        };
    }

    /**
     * Filter by ref.
     */
    public static Specification<Paa> ref(String ref) {
        return (root, query, criteriaBuilder) -> {
            if (ref == null || ref.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.like(root.get("ref"), "%" + ref + "%");
        };
    }

    /**
     * Filter by nom.
     */
    public static Specification<Paa> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            if (nom == null || nom.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.like(root.get("nom"), "%" + nom + "%");
        };
    }

    /**
     * Filter by annee.
     */
    public static Specification<Paa> annee(String annee) {
        return (root, query, criteriaBuilder) -> {
            if (annee == null || annee.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.equal(root.get("annee"), annee);
        };
    }

    /**
     * Filter by budgetEstime (exact match).
     */
    public static Specification<Paa> budgetEstime(BigDecimal budgetEstime) {
        return (root, query, criteriaBuilder) -> {
            if (budgetEstime == null) {
                return criteriaBuilder.conjunction(); // Return all if null
            }
            return criteriaBuilder.equal(root.get("budgetEstime"), budgetEstime);
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
     * Filter by ac.nom (Ac entity’s name).
     */
    public static Specification<Paa> acNom(String acNom) {
        return (root, query, criteriaBuilder) -> {
            if (acNom == null || acNom.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            Join<Paa, Ac> join = root.join("ac");
            return criteriaBuilder.like(join.get("nom"), "%" + acNom + "%");
        };
    }
}