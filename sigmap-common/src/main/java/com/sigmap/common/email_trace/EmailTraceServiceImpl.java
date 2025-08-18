package com.sigmap.common.email_trace;

import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.menu_item.MenuItemSpecifications;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Collection;

@Service
@Slf4j
@AllArgsConstructor
public class EmailTraceServiceImpl implements EmailTraceService {

    private final EmailTraceRepository emailTraceRepository;

    @Override
    public EmailTrace saveEmailTrace(String adminId,TypeEmail typeEmail, String objet, String contenu, String destinataire, boolean successSend, LocalDateTime dateSend) {
        try {
            EmailTrace emailTrace = new EmailTrace();
            emailTrace.setType_email(typeEmail);
            emailTrace.setObjet(objet);
            emailTrace.setContenu(contenu);
            emailTrace.setDestinataire(destinataire);
            emailTrace.setSuccessSend(successSend);
            emailTrace.setDate_send(dateSend);

            // Set audit fields
            emailTrace.setCreatedAt(Calendar.getInstance());
            emailTrace.setCreatedBy(adminId);

            
            EmailTrace savedEmailTrace = emailTraceRepository.save(emailTrace);
            log.info("Email trace saved successfully for type: {}, recipient: {}, success: {}", 
                    typeEmail, destinataire, successSend);
            return savedEmailTrace;
        } catch (Exception e) {
            log.error("Error saving email trace for type: {}, recipient: {}", typeEmail, destinataire, e);
            throw new RuntimeException("Failed to save email trace", e);
        }
    }


    @Override
    public EmailTrace updateEmailTrace(String adminId,TypeEmail typeEmail, String objet, String contenu, String destinataire, boolean successSend, LocalDateTime dateSend) {
        try {
            EmailTrace emailTrace = new EmailTrace();
            emailTrace.setType_email(typeEmail);
            emailTrace.setObjet(objet);
            emailTrace.setContenu(contenu);
            emailTrace.setDestinataire(destinataire);
            emailTrace.setSuccessSend(successSend);
            emailTrace.setDate_send(dateSend);

            // Set audit fields
            emailTrace.setUpdatedAt(Calendar.getInstance());
            emailTrace.setUpdatedBy(adminId);


            EmailTrace savedEmailTrace = emailTraceRepository.save(emailTrace);
            log.info("Email trace saved successfully for type: {}, recipient: {}, success: {}",
                    typeEmail, destinataire, successSend);
            return savedEmailTrace;
        } catch (Exception e) {
            log.error("Error saving email trace for type: {}, recipient: {}", typeEmail, destinataire, e);
            throw new RuntimeException("Failed to save email trace", e);
        }
    }

    @Override
    public Page<EmailTrace> findAll(Specification<EmailTrace> spec, Pageable pageable) {
        spec = spec.and(EmailTraceSpecification.deleted(false));
        return emailTraceRepository.findAll(spec, pageable);
    }
}
