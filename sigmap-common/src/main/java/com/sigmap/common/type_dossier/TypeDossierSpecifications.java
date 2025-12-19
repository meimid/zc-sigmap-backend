package com.sigmap.common.type_dossier;

import org.springframework.data.jpa.domain.Specification;

public class TypeDossierSpecifications {
    public static Specification<TypeDossier> search(String search) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + search + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("code"), likePattern),
                    criteriaBuilder.like(root.get("libelle"), likePattern),
                    criteriaBuilder.like(root.get("description"), likePattern)
            );
        };
    }

    /** id. */
    public static Specification<TypeDossier> id(Integer id) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"), id);
    }

    /** code. */
    public static Specification<TypeDossier> code(String code) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("code"), "%" + code + "%");
    }

    /** libelle. */
    public static Specification<TypeDossier> libelle(String libelle) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("libelle")),
                        "%" + libelle.toLowerCase() + "%"
                );
    }

    /** description. */
    public static Specification<TypeDossier> description(String description) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        "%" + description.toLowerCase() + "%"
                );
    }



}