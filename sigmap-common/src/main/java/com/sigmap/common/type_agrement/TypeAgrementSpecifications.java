package com.sigmap.common.type_agrement;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_types.RefType;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class TypeAgrementSpecifications {
    /** search. */
    public static Specification<TypeAgrement> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<TypeAgrement, RefParam> join = root.join("organismeDeDeliveranceAgrements");
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("libelle"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("description"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("codeColor"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("code"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("refType").get("description"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("refType").get("refCode"), "%" + search + "%"));
        };
    }

    /** id. */
    public static Specification<TypeAgrement> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** libelle. */
    public static Specification<TypeAgrement> libelle(String libelle) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("libelle"),libelle.toLowerCase());
        };
    }
    
    /** deleted. */
    public static Specification<TypeAgrement> deleted(boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (!deleted) {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("isDeleted")),
                        criteriaBuilder.isFalse(root.get("isDeleted")));
            }
            return criteriaBuilder.isTrue(root.get("isDeleted"));
        };
    }

}
