package com.sigmap.common.societe_blak_list;

import com.sigmap.common.activite.Activite;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.user_societe.UserSociete;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class SocieteBlackListSpecifications {

    public static Specification<SocieteBlackList> search(String search) {
        return (root, query, criteriaBuilder) -> {

            query.distinct(true);
            String like = "%" + search + "%";

            Join<SocieteBlackList, Societe> societeJoin =
                    root.join("societe", JoinType.LEFT);

            Join<Societe, Adresse> adresseJoin =
                    societeJoin.join("address", JoinType.LEFT);

            Join<Societe, RefParam> typeJoin =
                    societeJoin.join("type", JoinType.LEFT);

            Join<Societe, RefParam> categorieJoin =
                    societeJoin.join("categorieSoumissionnaire", JoinType.LEFT);

            Join<Adresse, RefParam> paysJoin =
                    adresseJoin.join("pays", JoinType.LEFT);

            Join<Societe, Activite> activiteJoin =
                    societeJoin.join("activites", JoinType.LEFT);

            Join<Societe, UserSociete> userSocieteJoin =
                    societeJoin.join("usersSociete", JoinType.LEFT);

            Expression<String> dateEffetFormatted = criteriaBuilder.function(
                    "DATE_FORMAT",
                    String.class,
                    root.get("dateEffet"),
                    criteriaBuilder.literal("%Y-%m-%d %H:%i:%s")
            );

            Expression<String> delaiFormatted = criteriaBuilder.function(
                    "DATE_FORMAT",
                    String.class,
                    root.get("delai"),
                    criteriaBuilder.literal("%Y-%m-%d %H:%i:%s")
            );



            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("id").get("id").as(String.class), like),
                    criteriaBuilder.like(root.get("id").get("societeId").as(String.class), like),
                    criteriaBuilder.like(root.get("natureDecision").as(String.class), like),
                    criteriaBuilder.like(root.get("reference").as(String.class), like),
                    criteriaBuilder.like(criteriaBuilder.lower(dateEffetFormatted), like.toLowerCase()),
                    criteriaBuilder.like(criteriaBuilder.lower(delaiFormatted), like.toLowerCase()),
                    criteriaBuilder.like(societeJoin.get("nom"), like),
                    criteriaBuilder.like(societeJoin.get("nif"), like),
                    criteriaBuilder.like(societeJoin.get("numCnss"), like),
                    criteriaBuilder.like(societeJoin.get("status").as(String.class), like),
                    criteriaBuilder.like(typeJoin.get("code"), like),
                    criteriaBuilder.like(typeJoin.get("description"), like),
                    criteriaBuilder.like(typeJoin.get("codeColor"), like),
                    criteriaBuilder.like(categorieJoin.get("code"), like),
                    criteriaBuilder.like(categorieJoin.get("description"), like),
                    criteriaBuilder.like(categorieJoin.get("codeColor"), like),
                    criteriaBuilder.like(adresseJoin.get("email"), like),
                    criteriaBuilder.like(adresseJoin.get("tel1"), like),
                    criteriaBuilder.like(adresseJoin.get("tel2"), like),
                    criteriaBuilder.like(adresseJoin.get("fax"), like),
                    criteriaBuilder.like(adresseJoin.get("adresse"), like),
                    criteriaBuilder.like(adresseJoin.get("compAdresse"), like),
                    criteriaBuilder.like(adresseJoin.get("codePostal"), like),
                    criteriaBuilder.like(adresseJoin.get("ville"), like),
                    criteriaBuilder.like(paysJoin.get("code"), like),
                    criteriaBuilder.like(paysJoin.get("description"), like),
                    criteriaBuilder.like(paysJoin.get("codeColor"), like),
                    criteriaBuilder.like(activiteJoin.get("libelle"), like),
                    criteriaBuilder.like(activiteJoin.get("code"), like),
                    criteriaBuilder.like(userSocieteJoin.get("userId"), like),
                    criteriaBuilder.like(userSocieteJoin.get("nom"), like),
                    criteriaBuilder.like(userSocieteJoin.get("prenom"), like),
                    criteriaBuilder.like(userSocieteJoin.get("numCin"), like),
                    criteriaBuilder.like(userSocieteJoin.get("tel1"), like),
                    criteriaBuilder.like(userSocieteJoin.get("tel2"), like),
                    criteriaBuilder.like(userSocieteJoin.get("email"), like)
            );
        };
    }


    // SIMPLE FILTERS
    public static Specification<SocieteBlackList> id(Integer id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id").get("id"), id);
    }

    public static Specification<SocieteBlackList> societeId(Integer societeId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id").get("societeId"), societeId);
    }

    /** deleted. */
    public static Specification<SocieteBlackList> deleted(boolean deleted) {
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