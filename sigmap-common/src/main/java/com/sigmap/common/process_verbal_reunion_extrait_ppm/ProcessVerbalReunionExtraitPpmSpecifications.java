package com.sigmap.common.process_verbal_reunion_extrait_ppm;

import com.sigmap.common.ppms.Ppm;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ProcessVerbalReunionExtraitPpmSpecifications {

    public static Specification<ProcessVerbalReunionExtraitPpm> byExtraitKey(
            Integer extraitId, Integer pvId, Integer acId
    ) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("extraitId"), extraitId),
                cb.equal(root.get("pvId"), pvId),
                cb.equal(root.get("acId"), acId)
        );
    }

    /** Optional: ensure linked PPM is not deleted (null treated as not deleted). */
    public static Specification<ProcessVerbalReunionExtraitPpm> ppmNotDeleted() {
        return (root, query, cb) -> {
            Join<ProcessVerbalReunionExtraitPpm, Ppm> ppmJoin = root.join("ppm", JoinType.INNER);
            return cb.or(cb.isNull(ppmJoin.get("isDeleted")), cb.isFalse(ppmJoin.get("isDeleted")));
        };
    }
}