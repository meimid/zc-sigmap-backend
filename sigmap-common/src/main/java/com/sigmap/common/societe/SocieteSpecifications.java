package com.sigmap.common.societe;

import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
/**
 * Specification for managing {@link Societe}.
 */
public class SocieteSpecifications {
    /** search. */
    public static Specification<Societe> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<Societe, RefParam> join = root.join("RefParam");
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("decretCreation"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("nni"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("categorieSoumissionnaireId"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("description"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("codeColor"),"%" + search + "%")

            );
        };
    }

    /** id. */
    public  static  Specification<Societe> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** nom. */
    public static Specification<Societe> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("nom"), "%" + nom + "%");
        };
    }
    /** nni. */
    public static Specification<Societe> nni(String nni) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("nni"), "%" + nni + "%");
        };
    }
    /** categorieSoumissionnaireId. */
    public static Specification<Societe> categorieSoumissionnaireId(String categorieSoumissionnaireId) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("categorieSoumissionnaireId"), "%" + categorieSoumissionnaireId + "%");
        };
    }



    /** deleted. */
    public static Specification<Societe> deleted(boolean deleted) {
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
