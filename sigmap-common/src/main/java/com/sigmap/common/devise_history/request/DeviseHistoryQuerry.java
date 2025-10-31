package com.sigmap.common.devise_history.request;

import com.sigmap.common.devise_history.DeviseHistory;
import com.sigmap.common.devise_history.DeviseHistorySpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import java.util.Calendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Data
public class DeviseHistoryQuerry {

    private String search;

    private String code;

    private String description;

    private String action;

    private String dateDebut;

    private String dateFin;

    private Integer deviseId;


    public Specification<DeviseHistory> toSpecification() {
        Specification<DeviseHistory> spec = Specification.where(null);

        // Convertir les chaînes de date en objets Calendar si elles sont fournies
        Calendar debut = parseDateStringToCalendar(dateDebut);
        Calendar fin = parseDateStringToCalendar(dateFin);

        // Utiliser la méthode de recherche avec les dates si au moins une date est fournie
        if (search != null && !search.isBlank() && (debut != null || fin != null)) {
            spec = spec.and(DeviseHistorySpecifications.search(search, debut, fin));
        } else if (search != null && !search.isBlank()) {
            spec = spec.and(DeviseHistorySpecifications.search(search));
        } else if (debut != null || fin != null) {
            // Si seule une date est fournie, utiliser la même valeur pour les deux
            Calendar startDate = debut != null ? debut : fin;
            Calendar endDate = fin != null ? fin : debut;
            spec = spec.and(DeviseHistorySpecifications.search(null, startDate, endDate));
        }

        // Ajouter les autres critères de recherche
        if (code != null && !code.isBlank()) {
            spec = spec.and(DeviseHistorySpecifications.code(code));
        }

        if (description != null && !description.isBlank()) {
            spec = spec.and(DeviseHistorySpecifications.description(description));
        }
        if (action != null && !action.isBlank()) {
            spec = spec.and(DeviseHistorySpecifications.action(action));
        }

        if(action != null && !action.isBlank()){
            spec = spec.and(DeviseHistorySpecifications.deviseId(deviseId));
        }


        return spec;
    }

    /**
     * Convertit une chaîne de date au format "jj/mm/aaaa" en objet Calendar
     * @param dateString la chaîne de date à convertir
     * @return un objet Calendar correspondant à la date, ou null si la conversion échoue
     */
    private Calendar parseDateStringToCalendar(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(dateString));
            return calendar;
        } catch (ParseException e) {
            // En cas d'erreur de parsing, ignorer et retourner null
            return null;
        }
    }
}

