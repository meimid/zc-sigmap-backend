package com.sigmap.common.delais_reglementaires;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.authority.Authority;
import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DelaisReglementairesSpecifications {

    public static Specification<DelaisReglementaires> search(String search) {
        return (root, query, criteriaBuilder) -> {

            // If search is null or empty, return no results
            if (search == null || search.trim().isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String like = "%" + search.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // Numeric fields: try equals when search is a number
            try {
                Integer value = Integer.valueOf(search.trim());
                predicates.add(criteriaBuilder.equal(root.get("delaiPublicationLancement"), value));
                predicates.add(criteriaBuilder.equal(root.get("delaiLancementAttribution"), value));
                predicates.add(criteriaBuilder.equal(root.get("delaiAttributionDemarrage"), value));
            } catch (NumberFormatException ignored) { }

            // Join via embedded id for associated entities
            Join<?, RefParam> typeMarcherJoin = root.join("id").join("typeMarcher", JoinType.LEFT);
            Join<?, ModeDePassationSelection> modePassationJoin = root.join("id").join("modePassation", JoinType.LEFT);

            predicates.add(
                criteriaBuilder.like(criteriaBuilder.lower(typeMarcherJoin.get("code")), like)
            );
            predicates.add(
                criteriaBuilder.like(criteriaBuilder.lower(modePassationJoin.get("libelle")), like)
            );

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }



    public static Specification<DelaisReglementaires> findById(Integer typeMarcherId, Integer modePassationId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by typeMarcherId
            if (typeMarcherId != null) {
                predicates.add(criteriaBuilder.equal(root.get("id").get("typeMarcher").get("id"), typeMarcherId));
            }

            // Filter by modePassationId
            if (modePassationId != null) {
                predicates.add(criteriaBuilder.equal(root.get("id").get("modePassation").get("id"), modePassationId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<DelaisReglementaires> typeMarcherId(Integer typeMarcherId) {
        return (root, query, criteriaBuilder) -> {
            if (typeMarcherId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("id").get("typeMarcher").get("id"), typeMarcherId);
        };
    }

    public static Specification<DelaisReglementaires> modePassationId(Integer modePassationId) {
        return (root, query, criteriaBuilder) -> {
            if (modePassationId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("id").get("modePassation").get("id"), modePassationId);
        };
    }

    public static Specification<DelaisReglementaires> delaiPublicationLancementMin(Integer delaiMin) {
        return (root, query, criteriaBuilder) -> {
            if (delaiMin == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("delaiPublicationLancement"), delaiMin);
        };
    }

    public static Specification<DelaisReglementaires> delaiPublicationLancementMax(Integer delaiMax) {
        return (root, query, criteriaBuilder) -> {
            if (delaiMax == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("delaiPublicationLancement"), delaiMax);
        };
    }

    public static Specification<DelaisReglementaires> delaiLancementAttributionMin(Integer delaiMin) {
        return (root, query, criteriaBuilder) -> {
            if (delaiMin == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("delaiLancementAttribution"), delaiMin);
        };
    }

    public static Specification<DelaisReglementaires> delaiLancementAttributionMax(Integer delaiMax) {
        return (root, query, criteriaBuilder) -> {
            if (delaiMax == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("delaiLancementAttribution"), delaiMax);
        };
    }

    public static Specification<DelaisReglementaires> delaiAttributionDemarrageMin(Integer delaiMin) {
        return (root, query, criteriaBuilder) -> {
            if (delaiMin == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("delaiAttributionDemarrage"), delaiMin);
        };
    }

    public static Specification<DelaisReglementaires> delaiAttributionDemarrageMax(Integer delaiMax) {
        return (root, query, criteriaBuilder) -> {
            if (delaiMax == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("delaiAttributionDemarrage"), delaiMax);
        };
    }

    public static Specification<DelaisReglementaires> deleted(boolean deleted) {
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
} 