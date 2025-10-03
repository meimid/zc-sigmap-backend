package com.sigmap.common.realisations;

import com.sigmap.common.config.RealisationStatus;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class RealisationSpecifications {

    /**
     * Search across the realisationEnvisagees field.
     */
    public static Specification<Realisation> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("realisationEnvisagees"), "%" + search + "%");
        };
    }

    /**
     * Filter by composite ID (realisation_id and realisation_ver).
     */
    public static Specification<Realisation> id(CompositeId id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("id"), id.getId()),
                    criteriaBuilder.equal(root.get("version"), id.getVersion())
            );
        };
    }

    /**
     * Filter by realisationEnvisagees.
     */
    public static Specification<Realisation> realisationEnvisagees(String realisationEnvisagees) {
        return (root, query, criteriaBuilder) -> {
            if (realisationEnvisagees == null || realisationEnvisagees.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("realisationEnvisagees"), "%" + realisationEnvisagees + "%");
        };
    }

    /**
     * Filter by deletion status (isDeleted field from BaseEntity).
     */
    public static Specification<Realisation> deleted(boolean deleted) {
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
     * Filter by serviceId.
     */
    public static Specification<Realisation> serviceId(Integer serviceId) {
        return (root, query, criteriaBuilder) -> {
            if (serviceId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("serviceId"), serviceId);
        };
    }

    /**
     * Filter by directionId.
     */
    public static Specification<Realisation> directionId(Integer directionId) {
        return (root, query, criteriaBuilder) -> {
            if (directionId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("directionId"), directionId);
        };
    }

    /**
     * Filter by status.
     */
    public static Specification<Realisation> status(RealisationStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("statut"), status);
        };
    }

    /**
     * Check if refParam is used as service.
     */
 /*   public static Specification<Realisation> hasServiceId(Integer serviceId) {
        return (root, query, cb) -> {
            Join<Realisation, RefParam> serviceJoin = root.join("service", JoinType.LEFT);
            return cb.and(
                    cb.equal(serviceJoin.get("id"), serviceId),
                    cb.equal(serviceJoin.get("isDeleted"), false)
            );
        };
    }*/

    /**
     * Check if refParam is used as direction.
     */
/*    public static Specification<Realisation> hasDirectionId(Integer directionId) {
        return (root, query, cb) -> {
            Join<Realisation, RefParam> directionJoin = root.join("direction", JoinType.LEFT);
            return cb.and(
                    cb.equal(directionJoin.get("id"), directionId),
                    cb.equal(directionJoin.get("isDeleted"), false)
            );
        };
    }*/

    /**
     * Check if refParam is used as sourceFinancement.
     */
/*    public static Specification<Realisation> hasSourceFinancementId(Integer sourceFiId) {
        return (root, query, cb) -> {
            Join<Realisation, RefParam> sfJoin = root.join("sourceFinancement", JoinType.LEFT);
            return cb.and(
                    cb.equal(sfJoin.get("id"), sourceFiId),
                    cb.equal(sfJoin.get("isDeleted"), false)
            );
        };
    }*/

    /**
     * Check if refParam is used as monais.
     */
    public static Specification<Realisation> hasMonaisId(Integer monaisId) {
        return (root, query, cb) -> {
            Join<Realisation, RefParam> monaisJoin = root.join("monais", JoinType.LEFT);
            return cb.and(
                    cb.equal(monaisJoin.get("id"), monaisId),
                    cb.equal(monaisJoin.get("isDeleted"), false)
            );
        };
    }

    /**
     * Check if refParam is used as typeMarcher.
     */
    public static Specification<Realisation> hasTypeMarcherId(Integer typeMarcherId) {
        return (root, query, cb) -> {
            Join<Realisation, RefParam> tmJoin = root.join("typeMarcher", JoinType.LEFT);
            return cb.and(
                    cb.equal(tmJoin.get("id"), typeMarcherId),
                    cb.equal(tmJoin.get("isDeleted"), false)
            );
        };
    }

    /**
     * Check if refParam is used as modeSelection.
     */
    public static Specification<Realisation> hasModeSelectionId(Integer modeSelectionId) {
        return (root, query, cb) -> {
            Join<Realisation, RefParam> msJoin = root.join("modeSelection", JoinType.LEFT);
            return cb.and(
                    cb.equal(msJoin.get("id"), modeSelectionId),
                    cb.equal(msJoin.get("isDeleted"), false)
            );
        };
    }

    /**
     * Check if refParam is used as imputationBudgetaire.
     */
    public static Specification<Realisation> hasImputationBudgetaireId(Integer imputationBudgetaireId) {
        return (root, query, cb) -> {
            Join<Realisation, RefParam> imputationBudgetaireJoin = root.join("imputationBudgetaire", JoinType.LEFT);
            return cb.and(
                    cb.equal(imputationBudgetaireJoin.get("id"), imputationBudgetaireId),
                    cb.equal(imputationBudgetaireJoin.get("isDeleted"), false)
            );
        };
    }

    /**
     * Combined specification that checks all possible uses of refParam in Realisation.
     */
    public static Specification<Realisation> hasRefParamId(Integer refParamId) {
        return

                hasMonaisId(refParamId)
                .or(hasTypeMarcherId(refParamId))
                .or(hasModeSelectionId(refParamId))
                .or(hasImputationBudgetaireId(refParamId));
    }
}