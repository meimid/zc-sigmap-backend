package com.sigmap.common.societe;

import com.sigmap.common.activite.Activite;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.entity.SocieteStatus;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
/**
 * Specification for managing {@link Societe}.
 */
public class SocieteSpecifications {
    /** search. */
    public static Specification<Societe> search(String search) {
        return (root, query, cb) -> {
            Join<Societe, RefParam> typeJoin = root.join("type", JoinType.LEFT);
            Join<Societe, RefParam> catJoin = root.join("categorieSoumissionnaire", JoinType.LEFT);
            Join<Societe, Activite> activiteJoin = root.join("activites", JoinType.LEFT);
            Join<Societe, Adresse> addressJoin = root.join("address", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("nom")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("nif")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("status")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("numCnss")), "%" + search.toLowerCase() + "%"),




                    cb.like(cb.lower(typeJoin.get("description")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(typeJoin.get("codeColor")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(typeJoin.get("code")), "%" + search.toLowerCase() + "%"),

                    cb.like(cb.lower(catJoin.get("description")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(catJoin.get("code")), "%" + search.toLowerCase() + "%"),

                    cb.like(cb.lower(activiteJoin.get("libelle")), "%" + search.toLowerCase() + "%"),

                    cb.like(cb.lower(addressJoin.get("email")),  "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(addressJoin.get("ville")),  "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(addressJoin.get("codePostal")),  "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(addressJoin.get("adresse")),  "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(addressJoin.get("tel1")),  "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(addressJoin.get("tel2")),  "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(addressJoin.get("fax")),  "%" + search.toLowerCase() + "%")
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

    public static Specification<Societe> hasPaysId(Integer paysId) {
        return (root, query, cb) -> cb.equal(root.get("pays").get("id"), paysId);
    }

    public static Specification<Societe> hasTypeId(Integer typeId) {
        return (root, query, cb) -> cb.equal(root.get("type").get("id"), typeId);
    }

    public static Specification<Societe> hasPaysIdOrTypeId(Integer refParamId) {
        return hasPaysId(refParamId).or(hasTypeId(refParamId));
    }

    public static Specification<Societe> nif(String nif) {
        return (root, query, cb) -> cb.like(root.get("nif"), "%" + nif + "%");
    }

    public static Specification<Societe> numCnss(String numCnss) {
        return (root, query, cb) -> cb.equal(root.get("numCnss"), numCnss);
    }

    public static Specification<Societe> dateNif(java.util.Date dateNif) {
        return (root, query, cb) -> cb.equal(root.get("dateNif"), dateNif);
    }

    public static Specification<Societe> status(SocieteStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Societe> hasActivite(Integer activiteId) {
        return (root, query, cb) -> {
            Join<Object, Object> activiteJoin = root.join("activites");
            return cb.equal(activiteJoin.get("id"), activiteId);
        };
    }

}
