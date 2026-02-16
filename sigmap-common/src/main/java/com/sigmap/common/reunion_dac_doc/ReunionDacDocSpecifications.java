package com.sigmap.common.reunion_dac_doc;

import com.sigmap.common.docs.Docs;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ReunionDacDocSpecifications {

    public static Specification<ReunionDacDoc> docId(Integer docId) {
        return (root, query, cb) -> {
            Join<ReunionDacDoc, Docs> docJoin = root.join("docs", JoinType.INNER);

            return cb.and(
                    cb.equal(docJoin.get("id"), docId),
                    cb.or(cb.isNull(docJoin.get("isDeleted")), cb.isFalse(docJoin.get("isDeleted")))
            );
        };
    }
}