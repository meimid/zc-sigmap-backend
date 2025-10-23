package com.sigmap.common.societe_address;

import com.sigmap.common.adresse.Adresse;
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
            Join<SocieteAddress, Adresse> joinAddress = root.join("address");


            return criteriaBuilder.or(
                    criteriaBuilder.like(join.get("code"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("description"), "%" + search + "%"),
                    criteriaBuilder.like(joinSociete.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(joinSociete.get("categorieSoumissionnaireId"), "%" + search + "%"),
                    criteriaBuilder.like(join.get("codeColor"),"%" + search + "%"),
                    criteriaBuilder.like(joinAddress.get("adresse"), "%" + search + "%"),
                    criteriaBuilder.like(joinAddress.get("compAdresse"), "%" + search + "%"),
                    criteriaBuilder.like(joinAddress.get("ville"), "%" + search + "%"),
                    criteriaBuilder.like(joinAddress.get("codePostal"), "%" + search + "%"),
                    criteriaBuilder.like(joinAddress.get("tel1"), "%" + search + "%"),
                    criteriaBuilder.like(joinAddress.get("tel2"), "%" + search + "%"),
                    criteriaBuilder.like(joinAddress.get("fax"), "%" + search + "%"),
                    criteriaBuilder.like(joinAddress.get("pays").get("description"), "%" + search + "%"),
                    criteriaBuilder.like(joinAddress.get("pays").get("code"), "%" + search + "%")


            );
        };
    }

    /** societe_id. */
    public  static  Specification<SocieteAddress> societeId(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("societeId"), id);
        };
    }

    /** adresse_id. */
    public  static  Specification<SocieteAddress> addressId(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("addressId"), id);
        };
    }

    /** address. */
    public static Specification<SocieteAddress> address(String address) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("address"), "%" + address + "%");
        };
    }



    public static Specification<SocieteAddress> hasTypeId(Integer typeId) {
        return (root, query, cb) -> cb.equal(root.get("type").get("id"), typeId);
    }
    /** 🔹 Filter by societe ID */
    public static Specification<SocieteAddress> hasSocieteId(Integer societeId) {
        return (root, query, cb) -> cb.equal(root.get("societe").get("id"), societeId);
    }

    /** 🔹 Filter by pays (from Adresse) */
    public static Specification<SocieteAddress> hasPaysId(Integer paysId) {
        return (root, query, cb) -> cb.equal(root.get("address").get("pays").get("id"), paysId);
    }

    /** 🔹 Filter by ville (from Adresse) */
    public static Specification<SocieteAddress> hasVille(String ville) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("address").get("ville")), "%" + ville.toLowerCase() + "%");
    }

    /** 🔹 Filter by code postal (from Adresse) */
    public static Specification<SocieteAddress> hasCodePostal(String codePostal) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("address").get("codePostal")), "%" + codePostal.toLowerCase() + "%");
    }


    /** 🔹 Filter by Type Description or Code (string-based) */
    public static Specification<SocieteAddress> hasTypeString(String type) {
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("type").get("description")), "%" + type.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("type").get("code")), "%" + type.toLowerCase() + "%")
        );
    }

    /** 🔹 Filter by Societe name (string-based) */
    public static Specification<SocieteAddress> hasSocieteName(String societeName) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("societe").get("nom")), "%" + societeName.toLowerCase() + "%");
    }

    /** 🔹 Filter by Pays name or code (string-based) */
    public static Specification<SocieteAddress> hasPaysString(String pays) {
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("address").get("pays").get("description")), "%" + pays.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("address").get("pays").get("code")), "%" + pays.toLowerCase() + "%")
        );
    }
    public static Specification<SocieteAddress> hasAdresse(String adresse) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("address").get("adresse")), "%" + adresse.toLowerCase() + "%");
    }

    public static Specification<SocieteAddress> hasCompAdresse(String compAdresse) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("address").get("compAdresse")), "%" + compAdresse.toLowerCase() + "%");
    }

    public static Specification<SocieteAddress> hasTel1(String tel1) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("address").get("tel1")), "%" + tel1.toLowerCase() + "%");
    }

    public static Specification<SocieteAddress> hasTel2(String tel2) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("address").get("tel2")), "%" + tel2.toLowerCase() + "%");
    }

    public static Specification<SocieteAddress> hasFax(String fax) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("address").get("fax")), "%" + fax.toLowerCase() + "%");
    }

}
