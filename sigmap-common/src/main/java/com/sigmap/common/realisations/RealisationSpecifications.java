package com.sigmap.common.realisations;

import com.sigmap.common.entity.CompositeId;
import org.springframework.data.jpa.domain.Specification;

public class RealisationSpecifications {

    /**
     * Search across the realisationEnvisagees field.
     */
    public static Specification<Realisation> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("realisationEnvisagees"), "%" + search + "%");
        };
    }

    /**
     * Filter by composite ID (realisation_id and realisation_ver).
     */
    public static Specification<Realisation> id(CompositeId id) {
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

    /**
     * Filter by realisationEnvisagees.
     */
    public static Specification<Realisation> realisationEnvisagees(String realisationEnvisagees) {
        return (root, query, criteriaBuilder) -> {
            if (realisationEnvisagees == null || realisationEnvisagees.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("realisationEnvisagees"), "%" + realisationEnvisagees + "%");
        };
    }

    /**
     * Filter by deletion status (isDeleted field from BaseEntity).
     */
    public static Specification<Realisation> deleted(boolean deleted) {
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

    /**
     * Filter by serviceId.
     */
    public static Specification<Realisation> serviceId(Integer serviceId) {
        return (root, query, criteriaBuilder) -> {
            if (serviceId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("serviceId"), serviceId);
        };
    }

    /**
     * Filter by directionId.
     */
    public static Specification<Realisation> directionId(Integer directionId) {
        return (root, query, criteriaBuilder) -> {
            if (directionId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("directionId"), directionId);
        };
    }
}

