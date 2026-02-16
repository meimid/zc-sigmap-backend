package com.sigmap.common.reunionPpm;

import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.reunion.Reunion;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ReunionPpmSpecifications {

    public static Specification<ReunionPpm> hasPpmId(Integer ppmId) {
        return (root, query, cb) ->
                ppmId == null ? cb.conjunction() : cb.equal(root.get("ppmId"), ppmId);
    }

    public static Specification<ReunionPpm> hasPpmVer(Integer ppmVer) {
        return (root, query, cb) ->
                ppmVer == null ? cb.conjunction() : cb.equal(root.get("ppmVer"), ppmVer);
    }

    public static Specification<ReunionPpm> reunionStatus(ReunionStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            Join<ReunionPpm, Reunion> r = root.join("reunion", JoinType.INNER);
            return cb.equal(r.get("status"), status);
        };
    }
}