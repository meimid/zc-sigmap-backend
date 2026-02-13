package com.sigmap.common.model_paa;

import org.springframework.data.jpa.domain.Specification;

public class ModelPaaSpecifications {
    public static Specification<ModelPaa> docId(Integer docId) {
        return (root, query, cb) -> {
            query.distinct(true);

            var docJoin = root.join("doc"); // ModelPpm.doc (Docs)

            return cb.and(
                    cb.equal(docJoin.get("id"), docId),
                    cb.or(cb.isNull(root.get("isDeleted")), cb.isFalse(root.get("isDeleted"))),
                    cb.or(cb.isNull(docJoin.get("isDeleted")), cb.isFalse(docJoin.get("isDeleted")))
            );
        };
    }
}
