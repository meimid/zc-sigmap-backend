package com.sigmap.common.lots;

import com.sigmap.common.entity.CompositeId;
import org.springframework.data.jpa.domain.Specification;

public class LotSpecifications {
    /** Search across description field. */
    public static Specification<Lot> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("description"), "%" + search + "%");
        };
    }

    /** Filter by composite key (lots_id and lots_ver). */
    public static Specification<Lot> id(CompositeId id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("id"), id.getId()),
                    criteriaBuilder.equal(root.get("version"), id.getVersion())
            );
        };
    }

    /** Filter by lots_ver. */
    public static Specification<Lot> version(Integer version) {
        return (root, query, criteriaBuilder) -> {
            if (version == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("version"), version);
        };
    }

    /** Filter by description. */
    public static Specification<Lot> description(String description) {
        return (root, query, criteriaBuilder) -> {
            if (description == null || description.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("description"), "%" + description + "%");
        };
    }

    /** Filter by deletion status. */
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