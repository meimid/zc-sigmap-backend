package com.sigmap.common.financement;

import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.realisations.Realisation;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

/**
 * Specifications for filtering Financement entities.
 * Provides static methods to create JPA Criteria API specifications
 * for querying Financement entities with various filters.
 */
public class FinancementSpecifications {

    /** Filter by composite key IDs */
    public static Specification<Financement> ids(Integer realisationId, Integer realisationVer, Integer monnaieId) {
        return (root, query, criteriaBuilder) -> {
            Join<Financement, FinancementId> idJoin = root.join("id");
            Join<FinancementId, Realisation> joinRealisation = idJoin.join("realisation");
            Join<FinancementId, Monnaie> joinMonnaie = idJoin.join("monnaie");

            Predicate realisationIdPredicate = criteriaBuilder.equal(joinRealisation.get("id"), realisationId);
            Predicate realisationVerPredicate = criteriaBuilder.equal(joinRealisation.get("version"), realisationVer);
            Predicate monnaieIdPredicate = criteriaBuilder.equal(joinMonnaie.get("id"), monnaieId);

            return criteriaBuilder.and(realisationIdPredicate, realisationVerPredicate, monnaieIdPredicate);
        };
    }

    /** Filter by realisation ID */
    public static Specification<Financement> hasRealisationId(Integer realisationId) {
        return (root, query, criteriaBuilder) -> {
            Join<Financement, FinancementId> idJoin = root.join("id");
            Join<FinancementId, Realisation> joinRealisation = idJoin.join("realisation");
            return criteriaBuilder.equal(joinRealisation.get("id"), realisationId);
        };
    }

    /** Filter by realisation version */
    public static Specification<Financement> hasRealisationVersion(Integer version) {
        return (root, query, criteriaBuilder) -> {
            Join<Financement, FinancementId> idJoin = root.join("id");
            Join<FinancementId, Realisation> joinRealisation = idJoin.join("realisation");
            return criteriaBuilder.equal(joinRealisation.get("version"), version);
        };
    }

    /** Filter by monnaie ID */
    public static Specification<Financement> hasMonnaieId(Integer monnaieId) {
        return (root, query, criteriaBuilder) -> {
            Join<Financement, FinancementId> idJoin = root.join("id");
            Join<FinancementId, Monnaie> joinMonnaie = idJoin.join("monnaie");
            return criteriaBuilder.equal(joinMonnaie.get("id"), monnaieId);
        };
    }

    /** Filter by source de financement ID */
    public static Specification<Financement> hasSourceDeFinancementId(Integer sourceDeFinancementId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("sourceDeFinancementId"), sourceDeFinancementId);
    }

    /** Filter by budget range */
    public static Specification<Financement> budgetEstimerBetween(BigDecimal min, BigDecimal max) {
        return (root, query, criteriaBuilder) -> {
            if (min != null && max != null) {
                return criteriaBuilder.between(root.get("budgetEstimer"), min, max);
            } else if (min != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("budgetEstimer"), min);
            } else if (max != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("budgetEstimer"), max);
            }
            return criteriaBuilder.conjunction(); // Always true if both are null
        };
    }

    /** Filter by minimum budget */
    public static Specification<Financement> budgetEstimerGreaterThan(BigDecimal amount) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("budgetEstimer"), amount);
    }

    /** Filter by maximum budget */
    public static Specification<Financement> budgetEstimerLessThan(BigDecimal amount) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("budgetEstimer"), amount);
    }

    /** Filter by deleted status */
    public static Specification<Financement> deleted(boolean deleted) {
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

    /** Filter non-deleted records */
    public static Specification<Financement> notDeleted() {
        return deleted(false);
    }
}
