package com.sigmap.common.doc_soumission;

import com.sigmap.common.dac_doc_soumission.DacDocSoumission;
import com.sigmap.common.type_dossier.TypeDossier;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class DocSoumissionSpecifications {
    /** search. */
    public static Specification<DocSoumission> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<DocSoumission, TypeDossier> typeJoin =
                    root.join("typeDossier", JoinType.LEFT);
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("nom")), "%" + search + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(typeJoin.get("code")), "%" + search + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(typeJoin.get("libelle")), "%" + search + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(typeJoin.get("description")), "%" + search + "%")
            );
        };
    }

    /** id. */
    public static Specification<DocSoumission> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /** nom. */
    public static Specification<DocSoumission> nom(String nom) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("nom"),nom.toLowerCase());
        };
    }

    /** typeDossier. */


    public static Specification<DocSoumission> typeDossier(String typeDossier) {
        return (root, query, criteriaBuilder) -> {

            if (typeDossier == null || typeDossier.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            Join<DocSoumission, TypeDossier> join =
                    root.join("typeDossier", JoinType.INNER);

            return criteriaBuilder.equal(
                    criteriaBuilder.upper(join.get("code")),
                    typeDossier.toUpperCase()
            );
        };
    }

    /** Documents linked to a given DAC */
    public static Specification<DocSoumission>notInDac (Integer dacId) {
        return (root, query, cb) -> {
            if (dacId == null) {
                return cb.conjunction();
            }
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<DacDocSoumission> subRoot = subquery.from(DacDocSoumission.class);
            subquery.select(subRoot.get("docSoumissionId"))
                    .where(cb.equal(subRoot.get("dacId"), dacId));

            return cb.not(root.get("id").in(subquery));
        };
    }

    /** Documents Not (IN) to a given DAC */
    public static Specification<DocSoumission> inDac (Integer dacId) {
        return (root, query, cb) -> {
            if (dacId == null) {
                return cb.conjunction();
            }
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<DacDocSoumission> subRoot = subquery.from(DacDocSoumission.class);
            subquery.select(subRoot.get("docSoumissionId"))
                    .where(cb.equal(subRoot.get("dacId"), dacId));

            return root.get("id").in(subquery);
        };
    }
    public static Specification<DocSoumission> hasTypeDossierId(Integer typeDossierId) {
        return (root, query, criteriaBuilder) -> {
            if (typeDossierId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    root.get("typeDossier").get("id"),
                    typeDossierId
            );
        };
    }

    public static Specification<DocSoumission> hasTypeDossierCode(String code) {
        return (root, query, criteriaBuilder) -> {

            if (code == null || code.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("typeDossier").get("code")),
                    code.toUpperCase()
            );
        };
    }



}
