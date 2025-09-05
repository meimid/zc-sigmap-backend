package com.sigmap.common.process_verbal_reunion;


import com.sigmap.common.docs.Docs;
import org.springframework.data.jpa.domain.Specification;

public class ProcessVerbalReunionSpecifications {
    public static Specification<ProcessVerbalReunion> search(String search) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(root.get("observation"), "%" + search + "%")

        );
    }


    /** id. */
    public  static  Specification<ProcessVerbalReunion> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /**
     * Filters by the observation.
     */
    public static Specification<ProcessVerbalReunion> observation(String observation) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("observation"), observation);
    }

    /**
     * Filters for records that are either soft-deleted or not.
     */
    public static Specification<ProcessVerbalReunion> deleted(boolean deleted) {
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