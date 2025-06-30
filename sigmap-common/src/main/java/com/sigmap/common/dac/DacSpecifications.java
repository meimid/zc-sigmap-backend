package com.sigmap.common.dac;


import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;


public class DacSpecifications {

    public static Specification<Dac> searchByDeleted(boolean deleted) {
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

    /**
     * Filter by dacId
     */
    public static Specification<Dac> id(Integer dacId, Integer dacVer) {
        return (root, query, criteriaBuilder) -> {
            if (dacId == null && dacVer == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("dacId"), dacId),
                    criteriaBuilder.equal(root.get("dacVer"), dacVer)
            );
        };
    }

    /**
     * Filter by dacVer
     */
    public static Specification<Dac> version(Integer dacVer) {
        return (root, query, criteriaBuilder) -> {
            if (dacVer == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("dacVer"), dacVer)
            );
        };
    }

    /**
     * Filter by Ppm
     */
    public static Specification<Dac> searchByPpm(Integer ppmId, Integer ppmVer) {
        return (root, query, criteriaBuilder) -> {
            if (ppmId == null && ppmVer == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Dac, Ppm> ppmJoin = root.join("ppm");
            if (ppmId != null && ppmVer != null) {
                return criteriaBuilder.and(
                        criteriaBuilder.equal(ppmJoin.get("ppmId"), ppmId),
                        criteriaBuilder.equal(ppmJoin.get("ppmVer"), ppmVer)
                );
            } else if (ppmId != null) {
                return criteriaBuilder.equal(ppmJoin.get("ppmId"), ppmId);
            } else {
                return criteriaBuilder.equal(ppmJoin.get("ppmVer"), ppmVer);
            }
        };
    }

    /**
     * Filter by Realisation
     */
    public static Specification<Dac> searchByRealisation(Integer realisationId, Integer realisationVer) {
        return (root, query, criteriaBuilder) -> {
            if (realisationId == null && realisationVer == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Dac, Realisation> realisationJoin = root.join("realisation");
            if (realisationId != null && realisationVer != null) {
                return criteriaBuilder.and(
                        criteriaBuilder.equal(realisationJoin.get("realisationId"), realisationId),
                        criteriaBuilder.equal(realisationJoin.get("realisationVer"), realisationVer)
                );
            } else if (realisationId != null) {
                return criteriaBuilder.equal(realisationJoin.get("realisationId"), realisationId);
            } else {
                return criteriaBuilder.equal(realisationJoin.get("realisationVer"), realisationVer);
            }
        };
    }

    /**
     * Filter by Category
     */
    public static Specification<Dac> searchByCategory(Integer categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Dac, RefParam> categoryJoin = root.join("category");
            return criteriaBuilder.equal(categoryJoin.get("refParamId"), categoryId);
        };
    }

    /**
     * Filter by Status
     */
    public static Specification<Dac> searchByStatus(ProcessStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
}
