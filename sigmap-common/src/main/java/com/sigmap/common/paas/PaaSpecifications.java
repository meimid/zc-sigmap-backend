package com.sigmap.common.paas;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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
                    criteriaBuilder.equal(root.get("id"), id.getId()),
                    criteriaBuilder.equal(root.get("version"), id.getVersion())
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
     * Filter by status.
     */
    public static Specification<Paa> status(RefParam status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction(); // Return all if null
            }
            return criteriaBuilder.equal(root.get("status"), status);
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
     * Filter by ac.nom (Ac entity's name).
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

    public static Specification<Paa> hasStatusId(Integer statusId) {
        return (root, query, cb) -> cb.equal(root.get("status").get("id"), statusId);
    }

    public static Specification<Paa> statusNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("status").get("isDeleted"), false);
    }

    public static Specification<Paa> isLatestVersion() {
        return (root, query, criteriaBuilder) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Paa> subRoot = subquery.from(Paa.class);
            subquery.select(criteriaBuilder.max(subRoot.get("version")))
                    .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                            criteriaBuilder.equal(subRoot.get("isDeleted"), false));
            return criteriaBuilder.equal(root.get("version"), subquery);
        };
    }
    public static Specification<Paa> paaAc(Integer acId) {
        return (root, query, cb) -> cb.equal(root.get("ac").get("id"), acId);
    }
}