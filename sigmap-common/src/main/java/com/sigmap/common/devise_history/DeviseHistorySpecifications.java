package com.sigmap.common.devise_history;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DeviseHistorySpecifications {

    /**
     * Search by text in code or description fields
     */
    public static Specification<DeviseHistory> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String searchTerm = search.trim().toLowerCase();
            String like = "%" + searchTerm + "%";

            Predicate codePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), like);
            Predicate descPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), like);
            
            return criteriaBuilder.or(codePredicate, descPredicate);
        };
    }

    /**
     * Search by text in code or description fields and filter by date range
     */
    public static Specification<DeviseHistory> search(String search, Calendar dateDebut, Calendar dateFin) {
        return search(search).and(dateRange(dateDebut, dateFin));
    }

    /**
     * Filter by date range
     * - If only dateDebut is provided: returns records where dateDebut is on the exact same day (ignoring time)
     * - If only dateFin is provided: returns records where dateFin is on the exact same day (ignoring time)
     * - If both are provided: returns records where dateDebut >= given dateDebut AND dateFin <= given dateFin
     */
    public static Specification<DeviseHistory> dateRange(Calendar dateDebut, Calendar dateFin) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (dateDebut != null && dateFin == null) {
                // When only dateDebut is provided, find records with exactly the same date (ignoring time)
                Calendar startOfDay = (Calendar) dateDebut.clone();
                startOfDay.set(Calendar.HOUR_OF_DAY, 0);
                startOfDay.set(Calendar.MINUTE, 0);
                startOfDay.set(Calendar.SECOND, 0);
                startOfDay.set(Calendar.MILLISECOND, 0);
                
                Calendar endOfDay = (Calendar) dateDebut.clone();
                endOfDay.set(Calendar.HOUR_OF_DAY, 23);
                endOfDay.set(Calendar.MINUTE, 59);
                endOfDay.set(Calendar.SECOND, 59);
                endOfDay.set(Calendar.MILLISECOND, 999);
                
                // Record's dateDebut is on the exact same day
                predicates.add(criteriaBuilder.between(root.get("dateDebut"), startOfDay, endOfDay));
            } else if (dateDebut != null) {
                // When both dateDebut and dateFin are provided, find records completely within the date range
                // Set start of the day for dateDebut
                Calendar startOfRange = (Calendar) dateDebut.clone();
                startOfRange.set(Calendar.HOUR_OF_DAY, 0);
                startOfRange.set(Calendar.MINUTE, 0);
                startOfRange.set(Calendar.SECOND, 0);
                startOfRange.set(Calendar.MILLISECOND, 0);
                
                // Set end of the day for dateFin
                Calendar endOfRange = (Calendar) dateFin.clone();
                endOfRange.set(Calendar.HOUR_OF_DAY, 23);
                endOfRange.set(Calendar.MINUTE, 59);
                endOfRange.set(Calendar.SECOND, 59);
                endOfRange.set(Calendar.MILLISECOND, 999);
                
                // Record's dateDebut is on or after the start of the range
                // AND record's dateFin is on or before the end of the range
                predicates.add(criteriaBuilder.and(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("dateDebut"), startOfRange),
                    criteriaBuilder.lessThanOrEqualTo(root.get("dateFin"), endOfRange)
                ));
            }
            
            if (dateFin != null && dateDebut == null) {
                // When only dateFin is provided, find records with exactly the same date (ignoring time)
                Calendar startOfDay = (Calendar) dateFin.clone();
                startOfDay.set(Calendar.HOUR_OF_DAY, 0);
                startOfDay.set(Calendar.MINUTE, 0);
                startOfDay.set(Calendar.SECOND, 0);
                startOfDay.set(Calendar.MILLISECOND, 0);
                
                Calendar endOfDay = (Calendar) dateFin.clone();
                endOfDay.set(Calendar.HOUR_OF_DAY, 23);
                endOfDay.set(Calendar.MINUTE, 59);
                endOfDay.set(Calendar.SECOND, 59);
                endOfDay.set(Calendar.MILLISECOND, 999);
                
                // Record's dateFin is on the exact same day
                predicates.add(criteriaBuilder.between(root.get("dateFin"), startOfDay, endOfDay));
            } else if (dateFin != null) {
                // This block is not needed anymore as the range is already handled above
                // when both dateDebut and dateFin are provided
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * description
     */
    public static Specification<DeviseHistory> description(String description) {
        return (root, query, cb) -> cb.like(root.get("description"), "%" + description + "%");
    }

    /**
     * code
     */

    /** code */
    public static Specification<DeviseHistory> code(String code) {
        return (root, query, cb) -> cb.like(root.get("code"), "%" + code + "%");
    }

    /** action */
    public static Specification<DeviseHistory> action(String action) {
        return (root, query, cb) -> cb.like(root.get("action"), "%" + action + "%");
    }

    /** deviseId */
    public static Specification<DeviseHistory> deviseId(Integer deviseId) {
        return (root, query, cb) -> cb.equal(root.get("deviseId"), deviseId);
    }

    public static Specification<DeviseHistory> dateBetween(Calendar targetDate) {
        return (root, query, cb) -> {
            if (targetDate == null) {
                return cb.conjunction();
            }

            // Set start of day (00:00:00.000)
            Calendar startOfDay = (Calendar) targetDate.clone();
            startOfDay.set(Calendar.HOUR_OF_DAY, 0);
            startOfDay.set(Calendar.MINUTE, 0);
            startOfDay.set(Calendar.SECOND, 0);
            startOfDay.set(Calendar.MILLISECOND, 0);

            // Set end of day (23:59:59.999)
            Calendar endOfDay = (Calendar) targetDate.clone();
            endOfDay.set(Calendar.HOUR_OF_DAY, 23);
            endOfDay.set(Calendar.MINUTE, 59);
            endOfDay.set(Calendar.SECOND, 59);
            endOfDay.set(Calendar.MILLISECOND, 999);

            // Find records where:
            // (dateDebut <= endOfDay) AND (dateFin >= startOfDay OR dateFin is null)
            return cb.and(
                    cb.lessThanOrEqualTo(root.get("dateDebut"), endOfDay),
                    cb.or(
                            cb.greaterThanOrEqualTo(root.get("dateFin"), startOfDay),
                            cb.isNull(root.get("dateFin"))
                    )
            );
        };
    }
}