package com.sigmap.common.email_trace.request;

import com.sigmap.common.direction.Direction;
import com.sigmap.common.direction.DirectionSpecifications;
import com.sigmap.common.email_trace.EmailTrace;
import com.sigmap.common.email_trace.EmailTraceSpecification;
import com.sigmap.common.email_trace.TypeEmail;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@Data
public class EmailTraceQuery {


    private Integer id;
    private TypeEmail type;
    private boolean deleted;
    private boolean successSend;
    private String destinataire;
    private LocalDateTime dateSend;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Specification<EmailTrace> toSpecification() {
        Specification<EmailTrace> spec = Specification.where(EmailTraceSpecification.deleted(deleted));

        if (id != null ) {
            spec = spec.and(EmailTraceSpecification.id(id));
        }
        if (type != null) {
            spec = spec.and(EmailTraceSpecification.type(type));
        }
        if (successSend ) {
            spec = spec.and(EmailTraceSpecification.successSend(successSend));
        }
        if (destinataire != null && !destinataire.isBlank()) {
            spec = spec.and(EmailTraceSpecification.destinataire(destinataire));
        }
        if (dateSend != null) {
            spec = spec.and(EmailTraceSpecification.dateSend(dateSend));
        }
        if (startDate != null || endDate != null) {
            spec = spec.and(EmailTraceSpecification.dateSendBetween(startDate, endDate));
        }
        return spec;
    }
}
