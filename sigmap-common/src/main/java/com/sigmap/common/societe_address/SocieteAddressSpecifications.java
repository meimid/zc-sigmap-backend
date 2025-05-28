package com.sigmap.common.societe_address;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification for managing {@link SocieteAddress}.
 */
public class SocieteAddressSpecifications {
    /** search. */
    public static Specification<SocieteAddress> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<SocieteAddress, RefParam> join = root.join("type");
            Join<SocieteAddress, Societe> joinSociete = root.join("societe");

            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("address"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("code"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("description"), "%" + search + "%"),
                    criteriaBuilder.like(joinSociete.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(joinSociete.get("nni"), "%" + search + "%"),
                    criteriaBuilder.like(joinSociete.get("categorieSoumissionnaireId"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("codeColor"),"%" + search + "%")


            );
        };
    }

    /** id. */
    public  static  Specification<SocieteAddress> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** address. */
    public static Specification<SocieteAddress> address(String address) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("address"), "%" + address + "%");
        };
    }

    /** deleted. */
    public static Specification<SocieteAddress> deleted(boolean deleted) {
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
