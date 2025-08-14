package com.sigmap.common.email_trace;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public interface EmailTraceService {
    
    /**
     * Save email trace record when sending an email
     * @param typeEmail The type of email being sent
     * @param objet The subject of the email
     * @param contenu The content of the email
     * @param destinataire The recipient email address
     * @param successSend Whether the email was sent successfully
     * @param dateSend The date when the email was sent
     * @return The saved EmailTrace entity
     */
    EmailTrace saveEmailTrace(String AdminId,TypeEmail typeEmail, String objet, String contenu, String destinataire, boolean successSend, LocalDateTime dateSend);

    EmailTrace updateEmailTrace(String AdminId,TypeEmail typeEmail, String objet, String contenu, String destinataire, boolean successSend, LocalDateTime dateSend);

    Page<EmailTrace> findAll(Specification<EmailTrace> spec, Pageable pageable);
}
