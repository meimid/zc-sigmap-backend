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

        // 🔍 Apply filters
        if (search != null && !search.isBlank()) {
            spec = spec.and(DeviseHistorySpecifications.search(search));
        }
        
        // Apply date filters if any date parameter is provided
        if (debut != null || fin != null) {
            spec = spec.and(DeviseHistorySpecifications.dateRange(debut, fin));
        }

        if (code != null && !code.isBlank()) {
            spec = spec.and(DeviseHistorySpecifications.code(code));
        }

        if (description != null && !description.isBlank()) {
            spec = spec.and(DeviseHistorySpecifications.description(description));
        }

        if (action != null && !action.isBlank()) {
            spec = spec.and(DeviseHistorySpecifications.action(action));
        }

        // ✅ FIXED: check deviseId properly
        if (deviseId != null) {
            spec = spec.and(DeviseHistorySpecifications.deviseId(deviseId));
        }

        return spec;
    }

    /** Convertit une chaîne "yyyy-MM-dd" en Calendar */
    private Calendar parseDateStringToCalendar(String dateString) {
        if (dateString == null || dateString.isBlank()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(dateString));
            return calendar;
        } catch (ParseException e) {
            return null;
        }
    }
}