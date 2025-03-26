package com.sigmap.common.lots;

import org.springframework.data.jpa.domain.Specification;

public class LotSpecifications {
    public static Specification<Lot> search(String search) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("description"), "%" + search + "%");
        };
    }

    public static Specification<Lot> id(LotsId id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("lotsId"), id.getLotsId()),
                    criteriaBuilder.equal(root.get("lotsVer"), id.getLotsVer())
            );
        };
    }

    public static Specification<Lot> description(String description) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("description"), "%" + description + "%");
        };
    }

    public static Specification<Lot> deleted(boolean deleted) {
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