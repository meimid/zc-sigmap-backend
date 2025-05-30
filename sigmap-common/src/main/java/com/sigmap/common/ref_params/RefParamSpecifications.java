package com.sigmap.common.ref_params;

import com.sigmap.common.ref_types.RefType;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification for managing {@link RefParam}.
 */
public class RefParamSpecifications {
    /** search. */
    public static Specification<RefParam> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<RefParam, RefType> join = root.join("refType");
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("code"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("description"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("description"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("codeColor"), "%" + search + "%")
            );
        };
    }

    /** id. */
    public  static  Specification<RefParam> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** description. */
    public static Specification<RefParam> description(String description) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("description"), "%" + description + "%");
        };
    }

    /** code. */
    public static Specification<RefParam> code(String code) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("code"), "%" + code + "%");
        };
    }

    /** deleted. */
    public static Specification<RefParam> deleted(boolean deleted) {
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
    /** code color */
    public static Specification<RefParam> codeColor(String codeColor) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("codeColor"), "%" + codeColor + "%");
        };
    }

}
