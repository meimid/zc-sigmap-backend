package com.sigmap.common.reunion;

import com.sigmap.common.com.Com;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.reunionPpm.ReunionPpm;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Calendar;
import java.util.List;

public class ReunionSpecifications {
    public static Specification<Reunion> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<Reunion, Com> comJoin = root.join("com", JoinType.LEFT);
            Join<Reunion, ReunionPpm> reunionPpmJoin = root.join("reunionPpms", JoinType.LEFT);
            Join<ReunionPpm, Ppm> ppmJoin = reunionPpmJoin.join("ppm", JoinType.LEFT);

            query.distinct(true);

            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("emplacement"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("object"), "%" + search + "%"),
                    criteriaBuilder.like(comJoin.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(ppmJoin.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(ppmJoin.get("ref"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("status"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("com").get("nom"), "%" + search + "%")


                    );
        };
    }

    /**
     * Filters by Reunion ID.
     */
    public static Specification<Reunion> id(Integer id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), id);
    }

    /**
     * Filters by the associated Com ID.
     */
    public static Specification<Reunion> comId(Integer comId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("com").get("id"), comId);
    }

    public static Specification<Reunion> comNom(String nom) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("com").get("nom"), nom);
    }


    /**
     * Filters by the ReunionStatus enum.
     */
    public static Specification<Reunion> status(ReunionStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    /**
     * Filters for reunions that have a specific PPM associated with them.
     */
    public static Specification<Reunion> hasPpm(Integer ppmId, Integer ppmVer) {
        return (root, query, criteriaBuilder) -> {
            Join<Reunion, Ppm> ppmJoin = root.join("ppms");
            return criteriaBuilder.and(
                    criteriaBuilder.equal(ppmJoin.get("id").get("id"), ppmId),
                    criteriaBuilder.equal(ppmJoin.get("id").get("version"), ppmVer)
            );
        };
    }

    /**
     * Filters for reunions that have a specific DAC associated with them.
     */
    public static Specification<Reunion> hasDac(Integer dacId, Integer dacVer) {
        return (root, query, criteriaBuilder) -> {
            Join<Reunion, Dac> dacJoin = root.join("dacs");
            return criteriaBuilder.and(
                    criteriaBuilder.equal(dacJoin.get("id").get("dacId"), dacId),
                    criteriaBuilder.equal(dacJoin.get("id").get("dacVer"), dacVer)
            );
        };
    }

    /**
     * Filters by reunionDate (greater than or equal to).
     */
    public static Specification<Reunion> reunionDateAfter(Calendar date) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("reunionDate"), date);
    }

    /**
     * Filters by reunionDate (less than or equal to).
     */
    public static Specification<Reunion> reunionDateBefore(Calendar date) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("reunionDate"), date);
    }

    /**
     * Filters for records that are either soft-deleted or not.
     */
    public static Specification<Reunion> deleted(boolean isDeleted) {
        return (root, query, criteriaBuilder) -> {
            if (isDeleted) {
                return criteriaBuilder.isTrue(root.get("isDeleted"));
            }
            // When looking for non-deleted items, also include those where the flag is null.
            return criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("isDeleted")),
                    criteriaBuilder.isFalse(root.get("isDeleted"))
            );
        };
    }

    public static Specification<Reunion> comIdSpec(List<Integer> comIds) {
        return (root, query, cb) -> {
            Join<Reunion, Com> comJoin = root.join("com");
            return comJoin.get("id").in(comIds);
        };
    }

    /**
     * Filter Reunion by PPM ID through ReunionPpm relationship.
     */
    public static Specification<Reunion> hasPpmId(Integer ppmId) {
        return (root, query, cb) -> {
            var join = root.join("reunionPpms"); // join Reunion -> ReunionPpm
            return cb.equal(join.get("ppmId"), ppmId);
        };
    }


}