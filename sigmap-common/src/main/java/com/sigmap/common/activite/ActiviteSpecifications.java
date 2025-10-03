package com.sigmap.common.activite;

import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class ActiviteSpecifications {
    /** search. */
    public static Specification<Activite> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<Activite, RefParam> join = root.join("categoriesActivites");
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("libelle"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("description"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("codeColor"), "%" + search + "%"));
        };
    }

    /** id. */
    public static Specification<Activite> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** libelle. */
    public static Specification<Activite> libelle(String libelle) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("libelle"),libelle.toLowerCase());
        };
    }



    /** deleted. */
    public static Specification<Activite> deleted(boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (!deleted) {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("isDeleted")),
                        criteriaBuilder.isFalse(root.get("isDeleted")));
            }
            return criteriaBuilder.isTrue(root.get("isDeleted"));
        };
    }

    public static Specification<Activite> hasActiviteId(Integer typeActiviteId) {
        return (root, query, cb) -> cb.equal(root.get("categoriesActivites").get("id"), typeActiviteId);
    }
}
