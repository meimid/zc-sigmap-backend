package com.sigmap.common.dac;


import org.springframework.data.jpa.domain.Specification;

public class DacSpecifications {

    public static Specification<Dac> searchByDeleted(boolean deleted) {
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
