package com.sigmap.common.user_commission_historique;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.reunion.exception.DateBefore;
import com.sigmap.common.user_commission_historique.exception.FutureDateNotAllowedException;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class UserCommissionHistoriqueSpecifications {
    public static Specification<UserCommissionHistorique> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction(); // no filter
            }


            return cb.or(
                    cb.like(cb.lower(root.get("userId")), "%" + search.trim().toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("status")), "%" + search.trim().toLowerCase() + "%"),
                    cb.like(cb.lower(cb.function("DATE_FORMAT", String.class, root.get("dateDebut"), cb.literal("%Y-%m-%d %H:%i"))), "%" + search.trim().toLowerCase() + "%"),
                    cb.like(cb.lower(cb.function("DATE_FORMAT", String.class, root.get("dateFin"), cb.literal("%Y-%m-%d %H:%i"))), "%" + search.trim().toLowerCase() + "%")
            );
        };
    }
    /** id. */
    public static Specification<UserCommissionHistorique> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }
    /** Filter by userId */
    public static Specification<UserCommissionHistorique> hasUserId(String userId) {
        return (root, query, cb) -> {
            if (userId == null || userId.isEmpty()) return cb.conjunction();
            return cb.equal(root.get("userId"), userId);
        };
    }

    /** Filter by commissionId */
    public static Specification<UserCommissionHistorique> hasComId(Integer comId) {
        return (root, query, cb) -> {
            if (comId == null) return cb.conjunction();
            return cb.equal(root.get("comId"), comId);
        };
    }

    /** Filter by status */
    public static Specification<UserCommissionHistorique> hasStatus(ActiveStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    /** Filter by dateDebut (greater than or equal) */
    public static Specification<UserCommissionHistorique> dateDebutAfter(LocalDateTime startDate) {
        return (root, query, cb) -> {
            if (startDate == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("dateDebut"), startDate);
        };
    }

    /** Filter by dateFin (less than or equal) */
    public static Specification<UserCommissionHistorique> dateFinBefore(LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (endDate == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("dateFin"), endDate);
        };
    }

    /** Filter between two dates */
    public static Specification<UserCommissionHistorique> betweenDates(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return cb.conjunction();
            if (start != null && end != null) {
                return cb.and(
                        cb.greaterThanOrEqualTo(root.get("dateDebut"), start),
                        cb.lessThanOrEqualTo(root.get("dateFin"), end)
                );
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("dateDebut"), start);
            } else {
                return cb.lessThanOrEqualTo(root.get("dateFin"), end);
            }
        };
    }

    /** Search by both userId and comId */
    public static Specification<UserCommissionHistorique> hasUserIdAndComId(String userId, Integer comId) {
        return (root, query, cb) -> {
            if ((userId == null || userId.isBlank()) && comId == null) {
                return cb.conjunction();
            }
            if (userId != null && comId != null) {
                return cb.and(
                        cb.equal(root.get("userId"), userId),
                        cb.equal(root.get("comId"), comId)
                );
            } else if (userId != null) {
                return cb.equal(root.get("userId"), userId);
            } else {
                return cb.equal(root.get("comId"), comId);
            }
        };
    }

    public static Specification<UserCommissionHistorique> hasExactDate(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();
            if(date.isAfter(LocalDate.now())) {
                throw new FutureDateNotAllowedException("");
            }
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            return cb.lessThanOrEqualTo(root.get("dateDebut"), endOfDay);

        };
    }

    public static Specification<UserCommissionHistorique> orderByIdDesc() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("id")));
            return cb.conjunction();
        };
    }


}
