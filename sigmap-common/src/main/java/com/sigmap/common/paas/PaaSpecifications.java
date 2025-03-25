package com.sigmap.common.paas;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ref_types.RefType;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class PaaSpecifications {
    /** Search across ref, nom, and ac.nom fields. */
    public static Specification<Paa> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<Paa, Ac> join = root.join("ac");
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("ref"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("nom"), "%" + search + "%") // Assumes Ac has 'nom'
            );
        };
    }

    /** Filter by paaId. */
    public static Specification<Paa> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("paaId"), id);
        };
    }

    /** Filter by nom. */
    public static Specification<Paa> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("nom"), "%" + nom + "%");
        };
    }

    /** Filter by ref. */
    public static Specification<Paa> ref(String ref) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("ref"), "%" + ref + "%");
        };
    }

    /** Filter by deletion status. */
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
}