package com.sigmap.common.user_historique_ac;

import com.sigmap.common.entity.ActiveStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class UserAcHistoriqueSpecifocations {

    /** userId */
    public static Specification<UserAcHistorique> userId(String userId) {
        return (root, query, cb) -> cb.like(root.get("userId"), "%" + userId + "%");
    }

    /** status */
    public static Specification<UserAcHistorique> status(ActiveStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** acId */
    public static Specification<UserAcHistorique> acId(Integer acId) {
        return (root, query, cb) -> cb.equal(root.get("ac").get("id"), acId);
    }

    /** dateRange - Find all records that overlap with the given date (from 00:00:00.000 to 23:59:59.999 of the given date) */
    public static Specification<UserAcHistorique> dateRange(Calendar targetDate) {
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
