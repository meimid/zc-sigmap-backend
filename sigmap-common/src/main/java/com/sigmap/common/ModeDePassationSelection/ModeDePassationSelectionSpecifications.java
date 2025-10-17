package com.sigmap.common.ModeDePassationSelection;

import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class ModeDePassationSelectionSpecifications{
    /** search. */
    public static Specification<ModeDePassationSelection> search(String search) {
        return (root, query, criteriaBuilder) -> {
        Join<ModeDePassationSelection, RefParam> join = root.join("typeMarchers");
            return criteriaBuilder.or(
                criteriaBuilder.like(root.get("libelle"), "%" + search + "%"),
                criteriaBuilder.like(join.get("description"), "%" + search + "%"),
                criteriaBuilder.like(join.get("codeColor"), "%" + search + "%"));
            };
    }

/** id. */
public static Specification<ModeDePassationSelection> id(Integer id) {
    return (root, query, criteriaBuilder) -> {
        return criteriaBuilder.equal(root.get("id"), id);
    };
}

/** libelle. */
public static Specification<ModeDePassationSelection> libelle(String libelle) {
    return (root, query, criteriaBuilder) -> {
        return criteriaBuilder.like(root.get("libelle"),libelle.toLowerCase());
    };
}



/** deleted. */
public static Specification<ModeDePassationSelection> deleted(boolean deleted) {
    return (root, query, criteriaBuilder) -> {
        if (!deleted) {
            return criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("isDeleted")),
                    criteriaBuilder.isFalse(root.get("isDeleted")));
        }
        return criteriaBuilder.isTrue(root.get("isDeleted"));
    };
}

public static Specification<ModeDePassationSelection> hastypeMarcherId(Integer typeMarcherId) {
    return (root, query, cb) -> cb.equal(root.get("typeMarchers").get("id"), typeMarcherId);
}

    /** isRestraint. */
    public static Specification<ModeDePassationSelection> isRestraint(boolean isRestraint) {
        return (root, query, criteriaBuilder) -> {
            if (!isRestraint) {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("isRestraint")),
                        criteriaBuilder.isFalse(root.get("isRestraint")));
            }
            return criteriaBuilder.isTrue(root.get("isRestraint"));
        };
    }
}
