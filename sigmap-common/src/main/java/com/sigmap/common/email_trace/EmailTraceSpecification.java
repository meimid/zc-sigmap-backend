package com.sigmap.common.email_trace;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.menu_item.MenuItem;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class EmailTraceSpecification {

    public static Specification<EmailTrace> id(Integer id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(

                    criteriaBuilder.equal(root.get("id"), id)
            );
        };
    }
    public static Specification<EmailTrace> deleted(boolean deleted) {
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


    public static Specification<EmailTrace> type (TypeEmail type_email) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("type_email"), type_email);
        };
    }

    public static Specification<EmailTrace> successSend (boolean successSend) {
        return (root, query, criteriaBuilder) -> {
            if (successSend) {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("success_send")),
                        criteriaBuilder.isFalse(root.get("success_send"))
                );
            }
            return criteriaBuilder.isTrue(root.get("success_send"));
        };
    }

    /*public static Specification<EmailTrace> successSenddd (boolean successSend) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("success_send"), successSend);
        };
    }*/

    public static Specification<EmailTrace> destinataire(String destinataire) {
        return (root, query, criteriaBuilder) -> {
            if (destinataire == null || destinataire.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("destinataire")),
                "%" + destinataire.toLowerCase() + "%"
            );
        };
    }

    public static Specification<EmailTrace> dateSend(LocalDateTime dateSend) {
        return (root, query, criteriaBuilder) -> {
            if (dateSend == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("date_send"), dateSend);
        };
    }

    public static Specification<EmailTrace> dateSendBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("date_send"), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("date_send"), startDate);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("date_send"), endDate);
        };
    }

    public static Specification<EmailTrace> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + search.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // Text fields
            predicates.add(cb.like(cb.lower(root.get("contenu")), like));
            predicates.add(cb.like(cb.lower(root.get("destinataire")), like));

            // Enum TypeEmail match by name
            try {
                TypeEmail type = TypeEmail.valueOf(search.trim().toUpperCase());
                predicates.add(cb.equal(root.get("type_email"), type));
            } catch (IllegalArgumentException ignored) {}

            // Boolean successSend
            if ("true".equalsIgnoreCase(search.trim()) || "false".equalsIgnoreCase(search.trim())) {
                boolean val = Boolean.parseBoolean(search.trim());
                predicates.add(cb.equal(root.get("success_send"), val));
            }

            // Date-time exact match if parsable
            try {
                LocalDateTime dt = LocalDateTime.parse(search.trim());
                predicates.add(cb.equal(root.get("date_send"), dt));
            } catch (Exception ignored) {}

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
