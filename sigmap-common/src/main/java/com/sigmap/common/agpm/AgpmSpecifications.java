package com.sigmap.common.agpm;

import com.sigmap.common.ac.AC;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification for managing {@link Agpm}.
 */
public class AgpmSpecifications {
    /** search. */
    public static Specification<Agpm> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<Agpm, AC> join = root.join("ac");
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("ref"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("content"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("name"), "%" + search + "%")
            );
        };
    }

    /** id. */
    public static Specification<Agpm> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** ref. */
    public static Specification<Agpm> ref(String ref) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("ref"), "%" + ref + "%");
        };
    }

    /** nom. */
    public static Specification<Agpm> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("nom"), "%" + nom + "%");
        };
    }

    /** deleted. */
    public static Specification<Agpm> deleted(boolean deleted) {
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