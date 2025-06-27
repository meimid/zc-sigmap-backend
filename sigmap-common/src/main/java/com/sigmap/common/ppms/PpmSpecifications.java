package com.sigmap.common.ppms;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class PpmSpecifications {
    /**
     * Search across ref, nom, and ac.nom fields.
     */
    public static Specification<Ppm> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if search is null/empty
            }
            Join<Ppm, Ac> join = root.join("ac");
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("ref")), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("nom")), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(join.get("nom")), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("status")), "%" + search.toLowerCase() + "%")
                    // Assumes Ac has 'nom'
            );
        };
    }

    public static Specification<Ppm> ppmAc(List<Integer> acIds) {
        return (root, query, cb) -> cb.in(root.get("ac").get("id")).value(acIds);
    }
    /**
     * Filter by ppm_id and ppm_ver.
     */
    public static Specification<Ppm> id(CompositeId id) {
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
     * Filter by status.
     */
    public static Specification<Ppm> status(ProcessStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return null; // Return all if null
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter by ppm_ver.
     */
    public static Specification<Ppm> version(Integer version) {
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
    public static Specification<Ppm> ref(String ref) {
        return (root, query, criteriaBuilder) -> {
            if (ref == null || ref.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("ref")), "%" + ref.toLowerCase() + "%");
        };
    }

    /**
     * Filter by nom.
     */
    public static Specification<Ppm> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            if (nom == null || nom.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("nom")), "%" + nom.toLowerCase() + "%");
        };
    }

    /**
     * Filter by annee.
     */
    public static Specification<Ppm> annee(String annee) {
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
    public static Specification<Ppm> budgetEstime(BigDecimal budgetEstime) {
        return (root, query, criteriaBuilder) -> {
            if (budgetEstime == null) {
                return criteriaBuilder.conjunction(); // Return all if null
            }
            return criteriaBuilder.equal(root.get("budgetEstime"), budgetEstime);
        };
    }

    /**
     * Filter by deletion status.
     */
    public static Specification<Ppm> deleted(boolean deleted) {
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
    public static Specification<Ppm> acNom(String acNom) {
        return (root, query, criteriaBuilder) -> {
            if (acNom == null || acNom.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            Join<Ppm, Ac> join = root.join("ac");
            return criteriaBuilder.like(criteriaBuilder.lower(join.get("nom")), "%" + acNom.toLowerCase() + "%");
        };
    }

    public static Specification<Ppm> hasModePassationId(Integer id) {
        return (root, query, cb) -> cb.equal(root.get("modePassation").get("id"), id);
    }

    public static Specification<Ppm> modePassationNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("modePassation").get("isDeleted"), false);
    }

    public static Specification<Ppm> isLatestVersion() {
        return (root, query, criteriaBuilder) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Ppm> subRoot = subquery.from(Ppm.class);
            subquery.select(criteriaBuilder.max(subRoot.get("version")))
                    .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                            criteriaBuilder.equal(subRoot.get("isDeleted"), false));
            return criteriaBuilder.equal(root.get("version"), subquery);
        };
    }
    public static Specification<Ppm> ppmAc(Integer acId) {
        return (root, query, cb) -> cb.equal(root.get("ac").get("id"), acId);
    }
}