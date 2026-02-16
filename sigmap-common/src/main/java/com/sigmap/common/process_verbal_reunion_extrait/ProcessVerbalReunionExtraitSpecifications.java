package com.sigmap.common.process_verbal_reunion_extrait;

import com.sigmap.common.docs.Docs;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ProcessVerbalReunionExtraitSpecifications {

    public static Specification<ProcessVerbalReunionExtrait> docId(Integer docId) {
        return (root, query, cb) -> {
            Join<ProcessVerbalReunionExtrait, Docs> docJoin = root.join("doc", JoinType.INNER);

            return cb.and(
                    cb.equal(docJoin.get("id"), docId),

                    // extrait not deleted (null treated as not deleted)
                    cb.or(cb.isNull(root.get("isDeleted")), cb.isFalse(root.get("isDeleted"))),

                    // doc not deleted (null treated as not deleted)
                    cb.or(cb.isNull(docJoin.get("isDeleted")), cb.isFalse(docJoin.get("isDeleted")))
            );
        };
    }
}