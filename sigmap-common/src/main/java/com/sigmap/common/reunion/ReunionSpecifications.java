package com.sigmap.common.reunion;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.appel_offre.AppelOffre;
import com.sigmap.common.com.Com;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.reunionDac.ReunionDac;
import com.sigmap.common.reunionPpm.ReunionPpm;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Calendar;
import java.util.List;

public class ReunionSpecifications {
    public static Specification<Reunion> search(String search) {
        return (root, query, criteriaBuilder) -> {
            Join<Reunion, Com> comJoin = root.join("com", JoinType.LEFT);
            Join<Reunion, ReunionPpm> reunionPpmJoin = root.join("reunionPpms", JoinType.LEFT);
            Join<ReunionPpm, Ppm> ppmJoin = reunionPpmJoin.join("ppm", JoinType.LEFT);
            Join<Ppm, Ac> acFromPpmJoin = ppmJoin.join("ac", JoinType.LEFT);
            Join<Reunion, ReunionDac> rdJoin = root.join("reunionDacs", JoinType.LEFT);
            Join<ReunionDac, Dac> dacJoin = rdJoin.join("dac", JoinType.LEFT);
            Join<Dac, AppelOffre> appelOffreJoin = dacJoin.join("appelOffre", JoinType.LEFT);
            Join<AppelOffre, Ppm> dacPpmJoin = appelOffreJoin.join("ppm", JoinType.LEFT);
            Join<Ppm, Ac> acFromDacJoin = dacPpmJoin.join("ac", JoinType.LEFT);

            query.distinct(true);
            Expression<String> formattedDate = criteriaBuilder.function(
                    "DATE_FORMAT",
                    String.class,
                    root.get("reunionDate"),
                    criteriaBuilder.literal("%Y-%m-%d %H:%i:%s")
            );

            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("emplacement"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("object"), "%" + search + "%"),
                    criteriaBuilder.like(comJoin.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(ppmJoin.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(ppmJoin.get("ref"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("status"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("note"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("com").get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(formattedDate), "%" + search + "%"),
                    criteriaBuilder.like(reunionPpmJoin.get("decision"), "%" + search + "%"),
                    /*criteriaBuilder.like(acFromPpmJoin.get("id"), "%" + search + "%"),
                    criteriaBuilder.like(acFromDacJoin.get("id"), "%" + search + "%"),*/
                    criteriaBuilder.like(acFromPpmJoin.get("nom"), "%" + search + "%"),
                    criteriaBuilder.like(acFromDacJoin.get("nom"), "%" + search + "%")


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

    public static Specification<Reunion> reunionNumCom(Integer reunionNumCom) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("reunionNumCom"), reunionNumCom);
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
    /*public static Specification<Reunion> hasPpmId(Integer ppmId) {
        return (root, query, cb) -> {
            var join = root.join("reunionPpms");
            return cb.equal(join.get("ppmId"), ppmId);
        };
    }

    public static Specification<Reunion> hasPpmVer(Integer ppmVer) {
        return (root, query, cb) -> {
            var join = root.join("reunionPpms");
            return cb.equal(join.get("ppmVer"), ppmVer);
        };
    }*/
    public static Specification<Reunion> hasPpmId(Integer ppmId) {
        return (root, query, cb) -> {
            query.distinct(true);

            // Via ReunionPpm
            Join<Reunion, ReunionPpm> reunionPpmJoin =
                    root.join("reunionPpms", JoinType.LEFT);
            Predicate viaReunionPpm =
                    cb.equal(reunionPpmJoin.get("ppmId"), ppmId);

            // Via ReunionDac -> Dac -> Ppm
            Join<Reunion, ReunionDac> reunionDacJoin =
                    root.join("reunionDacs", JoinType.LEFT);
            Join<ReunionDac, Dac> dacJoin =
                    reunionDacJoin.join("dac", JoinType.LEFT);
            Join<Dac, AppelOffre> appelOffreJoin =
                    dacJoin.join("appelOffre", JoinType.LEFT);
            Join<AppelOffre, Ppm> ppmJoin =
                    appelOffreJoin.join("ppm", JoinType.LEFT);

            Predicate viaReunionDacPpm =
                    cb.equal(ppmJoin.get("id"), ppmId);

            return cb.or(viaReunionPpm, viaReunionDacPpm);
        };
    }

    public static Specification<Reunion> hasPpmVer(Integer ppmVer) {
        return (root, query, cb) -> {
            query.distinct(true);

            // Via ReunionPpm
            Join<Reunion, ReunionPpm> reunionPpmJoin =
                    root.join("reunionPpms", JoinType.LEFT);
            Predicate viaReunionPpm =
                    cb.equal(reunionPpmJoin.get("ppmVer"), ppmVer);

            // Via ReunionDac -> Dac -> Ppm
            Join<Reunion, ReunionDac> reunionDacJoin =
                    root.join("reunionDacs", JoinType.LEFT);
            Join<ReunionDac, Dac> dacJoin =
                    reunionDacJoin.join("dac", JoinType.LEFT);
            Join<Dac, AppelOffre> appelOffreJoin =
                    dacJoin.join("appelOffre", JoinType.LEFT);
            Join<AppelOffre, Ppm> ppmJoin =
                    appelOffreJoin.join("ppm", JoinType.LEFT);

            Predicate viaReunionDacPpm =
                    cb.equal(ppmJoin.get("version"), ppmVer);

            return cb.or(viaReunionPpm, viaReunionDacPpm);
        };
    }

    public static Specification<Reunion> hasAcId(Integer acId) {
        return (root, query, cb) -> {
            query.distinct(true);

            // via ReunionPpm -> Ppm -> Ac
            Join<Reunion, ReunionPpm> rpJoin = root.join("reunionPpms", JoinType.LEFT);
            Join<ReunionPpm, Ppm> ppmJoin = rpJoin.join("ppm", JoinType.LEFT);
            Join<Ppm, Ac> acJoin = ppmJoin.join("ac", JoinType.LEFT);

            // via ReunionDac -> Dac -> Ppm -> Ac
            Join<Reunion, ReunionDac> rdJoin = root.join("reunionDacs", JoinType.LEFT);
            Join<ReunionDac, Dac> dacJoin = rdJoin.join("dac", JoinType.LEFT);
            Join<Dac, AppelOffre> appelOffreJoin = dacJoin.join("appelOffre", JoinType.LEFT);
            Join<AppelOffre, Ppm> dacPpmJoin = appelOffreJoin.join("ppm", JoinType.LEFT);
            Join<Ppm, Ac> acJoin2 = dacPpmJoin.join("ac", JoinType.LEFT);


            // PPM (from ReunionPpm path)
            var ppmNotDeleted = cb.or(
                    cb.isFalse(ppmJoin.get("isDeleted"))
            );

            // PPM (from DAC path)
            var dacPpmNotDeleted = cb.or(
                    cb.isFalse(dacPpmJoin.get("isDeleted"))
            );

            // DAC
            var dacNotDeleted = cb.or(
                    cb.isFalse(dacJoin.get("isDeleted"))
            );

            // AC from PPM
            var acNotDeleted = cb.or(
                    cb.isFalse(acJoin.get("isDeleted"))
            );

            // AC from DAC
            var ac2NotDeleted = cb.or(
                    cb.isFalse(acJoin2.get("isDeleted"))
            );

            // reunion is linked to this AC if:
            //  - path via PPM: AC matches + AC/PPM not deleted
            //  OR
            //  - path via DAC: AC matches + AC/PPM/DAC not deleted
            return cb.or(
                    cb.and(
                            acNotDeleted,
                            ppmNotDeleted,
                            cb.equal(acJoin.get("id"), acId)
                    ),
                    cb.and(
                            ac2NotDeleted,
                            dacPpmNotDeleted,
                            dacNotDeleted,
                            cb.equal(acJoin2.get("id"), acId)
                    )
            );
        };
    }

    public static Specification<Reunion> comTypeCPMP() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("com").get("comType").get("refType").get("code"), "CPMP");
    }

    public static Specification<Reunion> sortByComNom(Sort.Direction direction) {
        return (root, query, cb) -> {
            Join<Reunion, Com> comJoin = root.join("com", JoinType.LEFT);
            if (direction.isAscending()) {
                query.orderBy(cb.asc(comJoin.get("nom")));
            } else {
                query.orderBy(cb.desc(comJoin.get("nom")));
            }
            return null;
        };
    }
}