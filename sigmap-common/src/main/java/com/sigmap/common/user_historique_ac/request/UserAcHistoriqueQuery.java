package com.sigmap.common.user_historique_ac.request;

import com.sigmap.common.user_historique_ac.UserAcHistorique;
import com.sigmap.common.user_historique_ac.UserAcHistoriqueSpecifocations;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

@Data
public class UserAcHistoriqueQuery {

    private String userId;
    private String status;
    private Integer acId;
    private String date;

    public Specification<UserAcHistorique> toSpecification() {
        Specification<UserAcHistorique> spec = Specification.where(null);

        if (userId != null && !userId.isBlank()) {
            spec = spec.and(UserAcHistoriqueSpecifocations.userId(userId));
        }

        if (acId != null) {
            spec = spec.and(UserAcHistoriqueSpecifocations.acId(acId));
        }

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (date != null && !date.isBlank()) {
            try {
                // Parse the input date string in yyyy-MM-dd format
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                
                // Create calendar instance with UTC timezone
                Calendar targetDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                targetDate.setTime(sdf.parse(date));
                
                // Set time to start of day
                targetDate.set(Calendar.HOUR_OF_DAY, 0);
                targetDate.set(Calendar.MINUTE, 0);
                targetDate.set(Calendar.SECOND, 0);
                targetDate.set(Calendar.MILLISECOND, 0);
                
                // Add date range specification
                spec = spec.and(UserAcHistoriqueSpecifocations.dateRange(targetDate));
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid date format. Please use format: yyyy-MM-dd", e);
            }
        }

        return spec;
    }
}
