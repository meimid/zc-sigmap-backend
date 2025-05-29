package com.sigmap.common.compte_bancaire;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification for managing {@link CompteBancaire}.
 */
public class CompteBancaireSpecifications {
    public static Specification<CompteBancaire> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<CompteBancaire, RefParam> join = root.join("pays");
            Join<CompteBancaire, Societe> joinSociete = root.join("societe");
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("agence"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("rib"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("code"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("description"), "%" + search + "%"),
                    criteriaBuilder.like(joinSociete.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(joinSociete.get("nni"), "%" + search + "%"),
                    criteriaBuilder.like(joinSociete.get("categorieSoumissionnaireId"), "%" + search + "%"),                    criteriaBuilder.like(join.get("codeColor"),"%" + search + "%")

            );
        };
    }

    /** id. */
    public  static  Specification<CompteBancaire> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** nom. */
    public static Specification<CompteBancaire> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("nom"), "%" + nom + "%");
        };
    }
    /** agence. */
    public static Specification<CompteBancaire> agence(String agence) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("agence"), "%" + agence + "%");
        };
    }
    /** rib. */
    public static Specification<CompteBancaire> rib(String rib) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("rib"), "%" + rib + "%");
        };
    }



    /** deleted. */
    public static Specification<CompteBancaire> deleted(boolean deleted) {
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
