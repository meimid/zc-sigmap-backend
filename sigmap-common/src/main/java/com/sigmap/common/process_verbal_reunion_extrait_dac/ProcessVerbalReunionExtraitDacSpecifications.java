package com.sigmap.common.process_verbal_reunion_extrait_dac;

import com.sigmap.common.dac.Dac;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ProcessVerbalReunionExtraitDacSpecifications {

    public static Specification<ProcessVerbalReunionExtraitDac> byExtraitKey(
            Integer extraitId, Integer pvId, Integer acId
    ) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("extraitId"), extraitId),
                cb.equal(root.get("pvId"), pvId),
                cb.equal(root.get("acId"), acId)
        );
    }

    /** Optional: ensure linked DAC is not deleted (null treated as not deleted). */
    public static Specification<ProcessVerbalReunionExtraitDac> dacNotDeleted() {
        return (root, query, cb) -> {
            Join<ProcessVerbalReunionExtraitDac, Dac> dacJoin = root.join("dac", JoinType.INNER);
            return cb.or(cb.isNull(dacJoin.get("isDeleted")), cb.isFalse(dacJoin.get("isDeleted")));
        };
    }
}
