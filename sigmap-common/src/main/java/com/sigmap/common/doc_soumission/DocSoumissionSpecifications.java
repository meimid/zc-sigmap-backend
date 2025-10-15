package com.sigmap.common.doc_soumission;

import com.sigmap.common.config.TypeDossier;
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


}
