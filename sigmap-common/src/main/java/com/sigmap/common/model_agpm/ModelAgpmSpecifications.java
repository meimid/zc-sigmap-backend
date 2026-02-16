package com.sigmap.common.model_agpm;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.model_dac.ModelDac;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ModelAgpmSpecifications {
    public static Specification<ModelAgpm> docId(Integer docId) {
        return (root, query, cb) -> {
            Join<ModelDac, Docs> docJoin = root.join("doc", JoinType.INNER);
            return cb.and(
                    cb.equal(docJoin.get("id"), docId),
                    cb.or(cb.isNull(root.get("isDeleted")), cb.isFalse(root.get("isDeleted"))),
                    cb.or(cb.isNull(docJoin.get("isDeleted")), cb.isFalse(docJoin.get("isDeleted")))
            );
        };
    }
}
