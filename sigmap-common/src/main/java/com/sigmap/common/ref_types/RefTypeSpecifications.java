package com.sigmap.common.ref_types;

import org.springframework.data.jpa.domain.Specification;

/**
 * Specification for managing {@link RefType}.
 */
public class RefTypeSpecifications {
    /** search. */
    public static Specification<RefType> search(String search) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("description"), "%" + search + "%")
            );
        };
    }

    /** id. */
    public  static  Specification<RefType> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** description. */
    public static Specification<RefType> description(String description) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("description"), "%" + description + "%");
        };
    }

 

    /** deleted. */
    public static Specification<RefType> deleted(boolean deleted) {
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
