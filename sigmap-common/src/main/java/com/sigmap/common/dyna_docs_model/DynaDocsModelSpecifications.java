package com.sigmap.common.dyna_docs_model;

import com.sigmap.common.realisations.Realisation;
import org.springframework.data.jpa.domain.Specification;

public class DynaDocsModelSpecifications {


    public static Specification<DynaDocsModel> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("description"), "%" + search + "%");
        };
    }
    public static Specification<DynaDocsModel> dynaDocsModelId(Integer dynaDocsModelId) {
        return (root, query, criteriaBuilder) -> {
            if (dynaDocsModelId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(

                    criteriaBuilder.equal(root.get("dynaDocsModelId"), dynaDocsModelId)
            );
        };
    }

    public static Specification<DynaDocsModel> description(String description) {
        return (root, query, criteriaBuilder) -> {
            if (description == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(

                    criteriaBuilder.equal(root.get("description"), description)
            );
        };
    }

    public static Specification<DynaDocsModel> deleted(boolean deleted) {
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
