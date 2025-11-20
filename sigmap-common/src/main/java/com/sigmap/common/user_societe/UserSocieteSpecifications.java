package com.sigmap.common.user_societe;

import com.sigmap.common.activite.Activite;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification for managing {@link UserSociete}.
 */
public class UserSocieteSpecifications {

    /** search across user_societe fields and joined societe details */
    public static Specification<UserSociete> search(String search) {
        return (root, query, cb) -> {
            Join<UserSociete, Societe> societeJoin = root.join("societe", JoinType.LEFT);
            Join<Societe, RefParam> typeJoin = societeJoin.join("type", JoinType.LEFT);
            Join<Societe, RefParam> paysJoin = societeJoin.join("pays", JoinType.LEFT);
            Join<Societe, RefParam> categorieJoin = societeJoin.join("categorieSoumissionnaire", JoinType.LEFT);
            Join<Societe, Activite> activiteJoin = societeJoin.join("activites", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("userId")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("nom")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("prenom")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("numCin")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("tel1")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("tel2")), "%" + search.toLowerCase() + "%"),

                    cb.like(cb.lower(societeJoin.get("nom")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(societeJoin.get("nif")), "%" + search.toLowerCase() + "%"),

                    cb.like(cb.lower(typeJoin.get("description")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(paysJoin.get("description")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(categorieJoin.get("description")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(activiteJoin.get("libelle")), "%" + search.toLowerCase() + "%")
            );
        };
    }

    /** Filter by userId */
    public static Specification<UserSociete> userId(String userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    /** Filter by societeId */
    public static Specification<UserSociete> societeId(Integer societeId) {
        return (root, query, cb) -> cb.equal(root.get("societeId"), societeId);
    }

    /** Filter by responsable */
    public static Specification<UserSociete> responsable(Boolean responsable) {
        return (root, query, cb) -> cb.equal(root.get("responsable"), responsable);
    }

    /** Filter by ActiveStatus */
    public static Specification<UserSociete> status(ActiveStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** deleted flag */
    public static Specification<UserSociete> deleted(boolean deleted) {
        return (root, query, cb) -> {
            if (!deleted) {
                return cb.or(
                        cb.isNull(root.get("isDeleted")),
                        cb.isFalse(root.get("isDeleted"))
                );
            }
            return cb.isTrue(root.get("isDeleted"));
        };
    }

    /** Filter by societe relation */
    public static Specification<UserSociete> hasSocieteId(Integer societeId) {
        return (root, query, cb) -> {
            Join<UserSociete, Societe> societeJoin = root.join("societe", JoinType.LEFT);
            return cb.equal(societeJoin.get("id"), societeId);
        };
    }
}