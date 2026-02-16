package com.sigmap.common.reunionDac;

import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.reunion.Reunion;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ReunionDacSpecifications {

    public static Specification<ReunionDac> hasDacId(Integer dacId) {
        return (root, query, cb) ->
                dacId == null ? cb.conjunction() : cb.equal(root.get("dacId"), dacId);
    }

    public static Specification<ReunionDac> hasDacVer(Integer dacVer) {
        return (root, query, cb) ->
                dacVer == null ? cb.conjunction() : cb.equal(root.get("dacVer"), dacVer);
    }

    public static Specification<ReunionDac> reunionStatus(ReunionStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            Join<ReunionDac, Reunion> r = root.join("reunion", JoinType.INNER);
            return cb.equal(r.get("status"), status);
        };
    }
}