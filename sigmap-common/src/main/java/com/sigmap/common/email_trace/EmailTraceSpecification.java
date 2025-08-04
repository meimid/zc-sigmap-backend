package com.sigmap.common.email_trace;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.menu_item.MenuItem;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

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
}
