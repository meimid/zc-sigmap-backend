package com.sigmap.common.process_verbal_reunion;


import com.sigmap.common.docs.Docs;
import com.sigmap.common.process_verbal_reunion_doc.ProcessVerbalReunionDoc;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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

    public static Specification<ProcessVerbalReunion> docId(Integer docId) {
        return (root, query, cb) -> {
            query.distinct(true);

            // join PV -> documents (ProcessVerbalReunionDoc)
            Join<ProcessVerbalReunion, ProcessVerbalReunionDoc> pvrDocJoin =
                    root.join("documents", JoinType.INNER);

            // join ProcessVerbalReunionDoc -> doc (Docs)
            Join<ProcessVerbalReunionDoc, Docs> docJoin =
                    pvrDocJoin.join("doc", JoinType.INNER);

            return cb.and(
                    // match the requested doc
                    cb.equal(docJoin.get("id"), docId),

                    // the association row must be PV=true
                    cb.isTrue(pvrDocJoin.get("pv")),

                    // PV itself is not deleted (null treated as not deleted)
                    cb.or(cb.isNull(root.get("isDeleted")), cb.isFalse(root.get("isDeleted"))),

                    // Doc is not deleted (null treated as not deleted)
                    cb.or(cb.isNull(docJoin.get("isDeleted")), cb.isFalse(docJoin.get("isDeleted")))
            );
        };
    }
}