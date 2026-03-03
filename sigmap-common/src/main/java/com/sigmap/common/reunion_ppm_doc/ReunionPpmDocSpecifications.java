package com.sigmap.common.reunion_ppm_doc;

import com.sigmap.common.config.ReunionStatus;
import org.springframework.data.jpa.domain.Specification;

public class ReunionPpmDocSpecifications {

    public static Specification<ReunionPpmDoc> hasPpm(Integer ppmId, Integer ppmVer) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("ppmId"), ppmId),
                cb.equal(root.get("ppmVer"), ppmVer)
        );
    }

    public static Specification<ReunionPpmDoc> reunionClosedNotDeleted() {
        return (root, query, cb) -> {
            var rp = root.join("reunionPpm");      // ReunionPpmDoc -> ReunionPpm
            var r  = rp.join("reunion");           // ReunionPpm -> Reunion
            return cb.and(
                    cb.or(cb.isNull(r.get("isDeleted")), cb.isFalse(r.get("isDeleted"))),
                    cb.equal(r.get("status"), ReunionStatus.CLOTURER)
            );
        };
    }

    public static Specification<ReunionPpmDoc> hasAlfresco() {
        return (root, query, cb) -> {
            var d = root.join("docs");
            return cb.isNotNull(d.get("alfresco_id"));
        };
    }
}
