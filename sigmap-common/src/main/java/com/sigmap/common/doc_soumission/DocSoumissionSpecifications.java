package com.sigmap.common.doc_soumission;

import com.sigmap.common.config.TypeDossier;
import com.sigmap.common.dac_doc_soumission.DacDocSoumission;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class DocSoumissionSpecifications {
    /** search. */
    public static Specification<DocSoumission> search(String search) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("typeDossier"), "%" + search + "%"));
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
            try {
                TypeDossier dossierType = TypeDossier.valueOf(typeDossier.toUpperCase());
                return criteriaBuilder.equal(root.get("typeDossier"), dossierType);
            } catch (IllegalArgumentException e) {
                // If the provided type doesn't match any enum constant, return false condition
                return criteriaBuilder.disjunction();
            }
        };
    }
    /** Documents NOT linked to a given DAC */
    public static Specification<DocSoumission> notInDac(Integer dacId) {
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

    /** Documents linked (IN) to a given DAC */
    public static Specification<DocSoumission> inDac(Integer dacId) {
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


}
