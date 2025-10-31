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

    // Méthode de recherche avec les trois paramètres
    public static Specification<DeviseHistory> search(String search, Calendar dateDebut, Calendar dateFin) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Créer des copies finales pour les utiliser dans la lambda
            final Calendar startDate = dateDebut != null ? (Calendar) dateDebut.clone() : null;
            final Calendar endDate = dateFin != null ? (Calendar) dateFin.clone() : null;

            // Recherche textuelle si un terme de recherche est fourni
            if (search != null && !search.isBlank()) {
                String searchTerm = search.trim().toLowerCase();
                String like = "%" + searchTerm + "%";
                
                // Recherche dans les champs texte
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), like));
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), like));
                
                // Recherche par date au format jj/mm/aaaa
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date searchDate = sdf.parse(searchTerm);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(searchDate);
                    
                    // Créer un intervalle d'une journée pour la recherche
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
                    
                    // Recherche dans les deux champs de date
                    predicates.add(criteriaBuilder.between(root.get("dateDebut"), startOfDay, endOfDay));
                    predicates.add(criteriaBuilder.between(root.get("dateFin"), startOfDay, endOfDay));
                } catch (ParseException e) {
                    // Si le format de date n'est pas valide, on ignore cette condition
                }
            }
            
            // Filtrage par plage de dates si les deux dates sont fournies
            if (startDate != null && endDate != null) {
                // Créer des copies pour éviter de modifier les originaux
                Calendar start = (Calendar) startDate.clone();
                Calendar end = (Calendar) endDate.clone();
                
                // S'assurer que start est avant ou égal à end
                if (start.after(end)) {
                    Calendar temp = start;
                    start = end;
                    end = temp;
                }
                
                // Ajuster les heures pour couvrir toute la journée
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);
                
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 59);
                end.set(Calendar.MILLISECOND, 999);
                
                // Vérifier si les dates se chevauchent avec les plages existantes
                Predicate datePredicate = criteriaBuilder.or(
                    // dateDebut est dans l'intervalle
                    criteriaBuilder.between(root.get("dateDebut"), start, end),
                    // dateFin est dans l'intervalle
                    criteriaBuilder.between(root.get("dateFin"), start, end),
                    // L'intervalle contient complètement la plage de dates
                    criteriaBuilder.and(
                        criteriaBuilder.lessThanOrEqualTo(root.get("dateDebut"), start),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("dateFin"), end)
                    )
                );
                
                predicates.add(datePredicate);
            }
            
            // Si aucun prédicat n'a été ajouté, retourner une condition toujours vraie
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
    
    // Surcharge de la méthode pour la compatibilité avec le code existant
    public static Specification<DeviseHistory> search(String search) {
        return search(search, null, null);
    }

    /** description. */
    public static Specification<DeviseHistory> description(String description) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("description"), "%" + description + "%");
        };
    }


    /** code */
    public static Specification<DeviseHistory> code(String code) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("code"), "%" + code + "%" );
        };
    }

    /** action */
    public static Specification<DeviseHistory> action(String action) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("action"), "%" + action + "%" );
        };
    }

    public static Specification<DeviseHistory> deviseId(Integer deviseId) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("deviseId"),deviseId  );
        };
    }
}
