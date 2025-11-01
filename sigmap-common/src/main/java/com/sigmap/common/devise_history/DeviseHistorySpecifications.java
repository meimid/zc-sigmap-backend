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

    /** Recherche globale avec texte + filtre de dates */
    public static Specification<DeviseHistory> search(String search, Calendar dateDebut, Calendar dateFin) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            final Calendar startDate = dateDebut != null ? (Calendar) dateDebut.clone() : null;
            final Calendar endDate = dateFin != null ? (Calendar) dateFin.clone() : null;

            // 🔍 Recherche textuelle
            if (search != null && !search.isBlank()) {
                String searchTerm = search.trim().toLowerCase();
                String like = "%" + searchTerm + "%";

                Predicate codePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), like);
                Predicate descPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), like);
                predicates.add(criteriaBuilder.or(codePredicate, descPredicate));

                // Recherche si search est une date
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date searchDate = sdf.parse(searchTerm);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(searchDate);

                    Calendar startOfDay = (Calendar) calendar.clone();
                    startOfDay.set(Calendar.HOUR_OF_DAY, 0);
                    startOfDay.set(Calendar.MINUTE, 0);
                    startOfDay.set(Calendar.SECOND, 0);
                    startOfDay.set(Calendar.MILLISECOND, 0);

                    Calendar endOfDay = (Calendar) calendar.clone();
                    endOfDay.set(Calendar.HOUR_OF_DAY, 23);
                    endOfDay.set(Calendar.MINUTE, 59);
                    endOfDay.set(Calendar.SECOND, 59);
                    endOfDay.set(Calendar.MILLISECOND, 999);

                    Predicate dateDebutPredicate = criteriaBuilder.between(root.get("dateDebut"), startOfDay, endOfDay);
                    Predicate dateFinPredicate = criteriaBuilder.between(root.get("dateFin"), startOfDay, endOfDay);
                    predicates.add(criteriaBuilder.or(dateDebutPredicate, dateFinPredicate));
                } catch (ParseException ignored) {
                }
            }

            // 📅 Filtrage par plage de dates
            if (startDate != null && endDate != null) {
                Calendar start = (Calendar) startDate.clone();
                Calendar end = (Calendar) endDate.clone();

                if (start.after(end)) {
                    Calendar temp = start;
                    start = end;
                    end = temp;
                }

                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);

                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 59);
                end.set(Calendar.MILLISECOND, 999);

                // ✅ Strict containment: only records fully inside [start, end]
                Predicate datePredicate = criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("dateDebut"), start),
                        criteriaBuilder.lessThanOrEqualTo(root.get("dateFin"), end)
                );

                predicates.add(datePredicate);
            }
            
            // Si aucun prédicat n'a été ajouté, retourner une condition toujours vraie
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction(); // always true
            }

            // ✅ Combine all with AND for exact filtering
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Surcharge simple pour compatibilité */
    public static Specification<DeviseHistory> search(String search) {
        return search(search, null, null);
    }

    /** description */
    public static Specification<DeviseHistory> description(String description) {
        return (root, query, cb) -> cb.like(root.get("description"), "%" + description + "%");
    }


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
}