package com.sigmap.common.adresse;

import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class AdressSpecifications {
    public static Specification<Adresse> search(String search) {

        return (root, query, criteriaBuilder) -> {
            Join<Adresse, RefParam> join = root.join("pays");

            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("tel1"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("tel2"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("fax"), "%" + search + "%") ,
                    criteriaBuilder.like(root.get("adresse"), "%" + search + "%") ,
                    criteriaBuilder.like(root.get("compAdresse"), "%" + search + "%") ,
                    criteriaBuilder.like(root.get("codePostal"), "%" + search + "%") ,
                    criteriaBuilder.like(root.get("ville"), "%" + search + "%") ,
                    criteriaBuilder.like(join.get("description"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("codeColor"),"%" + search + "%"),
                    criteriaBuilder.like(join.get("code"),"%" + search + "%"));
        };
    }

    /** id. */
    public static Specification<Adresse> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** tel1. */
    public static Specification<Adresse> tel1(String tel1) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("tel1"), tel1 );
        };
    }

    /** tel2. */
    public static Specification<Adresse> tel2(String nom) {

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                criteriaBuilder.lower(root.get("tel2")),
                nom.toLowerCase()
        );
    }



    /** fax. */
    public static Specification<Adresse> fax(String fax) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("fax"),  fax );
        };
    }

    /** adresse. */
    public static Specification<Adresse> adresse(String adresse) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("adresse"), adresse);
        };
    }

    /** compAdresse. */
    public static Specification<Adresse> compAdresse(String compAdresse) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("compAdresse"), compAdresse);
        };
    }
    /** codePostal. */
    public static Specification<Adresse> codePostal(String codePostal) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("codePostal"), codePostal);
        };
    }
    /** ville. */
    public static Specification<Adresse> ville(String ville) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("ville"), ville);
        };
    }

    /** deleted. */
    public static Specification<Adresse> deleted(boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (!deleted) {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("isDeleted")),
                        criteriaBuilder.isFalse(root.get("isDeleted")));
            }
            return criteriaBuilder.isTrue(root.get("isDeleted"));
        };
    }
    public static Specification<Adresse> hasPaysId(Integer paysId) {
        return (root, query, cb) -> {
            Join<Adresse, RefParam> serviceJoin = root.join("pays", JoinType.LEFT);
            return cb.and(
                    cb.equal(serviceJoin.get("id"), paysId),
                    cb.equal(serviceJoin.get("isDeleted"), false)
            );
        };
    }
}
