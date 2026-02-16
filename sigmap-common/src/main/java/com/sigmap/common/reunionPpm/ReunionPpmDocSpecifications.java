package com.sigmap.common.reunionPpm;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDoc;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ReunionPpmDocSpecifications {

    public static Specification<ReunionPpmDoc> docId(Integer docId) {
        return (root, query, cb) -> {
            Join<ReunionPpmDoc, Docs> docJoin = root.join("docs", JoinType.INNER);

            return cb.and(
                    cb.equal(docJoin.get("id"), docId),
                    cb.or(cb.isNull(docJoin.get("isDeleted")), cb.isFalse(docJoin.get("isDeleted")))
            );
        };
    }
}
