package com.sigmap.common.ppms;

import com.sigmap.common.ac.Ac;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class PpmSpecifications {
    /** Search across ref, nom, and ac.nom fields. */
    public static Specification<Ppm> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<Ppm, Ac> join = root.join("ac");
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("ref"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("nom"), "%" + search + "%") // Assumes Ac has a nom field
            );
        };
    }

    /** Filter by ppmId. */
    public static Specification<Ppm> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("ppmId"), id);
        };
    }

    /** Filter by nom (similar to description). */
    public static Specification<Ppm> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("nom"), "%" + nom + "%");
        };
    }

    /** Filter by ref (similar to code). */
    public static Specification<Ppm> ref(String ref) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("ref"), "%" + ref + "%");
        };
    }

    /** Filter by deletion status. */
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
}
