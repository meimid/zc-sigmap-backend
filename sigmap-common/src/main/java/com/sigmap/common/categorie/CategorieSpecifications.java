package com.sigmap.common.categorie;

import org.springframework.data.jpa.domain.Specification;



public class CategorieSpecifications {

    public static Specification<Categorie> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if search is null/empty
            }
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("label"), "%" + search + "%")
            );
        };
    }

    /**
     * Filter by ID
     */
    public static Specification<Categorie> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(

                    criteriaBuilder.equal(root.get("id"), id)
            );
        };
    }

    /**
     * Filter by label.
     */
    public static Specification<Categorie> label(String label) {
        return (root, query, criteriaBuilder) -> {
            if (label == null || label.isBlank()) {
                return criteriaBuilder.conjunction(); // Return all if null/empty
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("label")), "%" + label.toLowerCase() + "%");
        };
    }

    /**
     * Filter by deleted status (isDeleted field from BaseEntity).
     */
    public static Specification<Categorie> deleted(boolean deleted) {
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
