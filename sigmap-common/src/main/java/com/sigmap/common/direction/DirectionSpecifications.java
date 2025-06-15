package com.sigmap.common.direction;

import com.sigmap.common.ac.Ac;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
public class DirectionSpecifications {
    public static Specification<Direction> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<Direction, Ac> join = root.join("acs");

            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("libelle"), "%" + search + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("ref")), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("nom")), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(join.get("nom")), "%" + search.toLowerCase() + "%"));
        };
    }
    /** id. */
    public static Specification<Direction> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** libelle. */
    public static Specification<Direction> libelle(String libelle) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                criteriaBuilder.lower(root.get("libelle")),
                libelle.toLowerCase()
        );
    }
    /** deleted. */
    public static Specification<Direction> deleted(boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (!deleted) {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("isDeleted")),
                        criteriaBuilder.isFalse(root.get("isDeleted")));
            }
            return criteriaBuilder.isTrue(root.get("isDeleted"));
        };
    }

    /**
     * Creates a Specification to find Directions associated with a specific Ac ID.
     * This works for a ManyToMany relationship.
     *
     * @param acId The ID of the Ac to filter by.
     * @return A Specification<Direction>. Returns a non-filtering specification if acId is null.
     */
    public static Specification<Direction> hasAcWithId(Integer acId) {
        // If no ID is provided, return a specification that does nothing.
        if (acId == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        return (root, query, criteriaBuilder) -> {
            // Join from Direction (root) to the collection of Ac entities ("acs").
            Join<Direction, Ac> acJoin = root.join("acs");

            // Create a predicate where the ID of the joined Ac ("id") must equal the provided acId.
            return criteriaBuilder.equal(acJoin.get("id"), acId);
        };
    }
}

