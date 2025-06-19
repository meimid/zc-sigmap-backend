package com.sigmap.common.direction;

import com.sigmap.common.ac.Ac;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
public class DirectionSpecifications {
    public static Specification<Direction> search(String search) {
        return (root, query, criteriaBuilder) -> {

            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("libelle"), "%" + search + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("ref")), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("nom")), "%" + search.toLowerCase() + "%"));
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
    public static Specification<Direction> isMemberOfAc(Integer acId) {
        if (acId == null) {
            return null; // Return null to not add a filter if no ID is provided
        }
        // This is now CORRECT because the Direction class has a field named "acId"
        return (root, query, cb) -> cb.equal(root.get("acId"), acId);
    }
}

