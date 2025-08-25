package com.sigmap.sigmap_params.email_trace;

import com.sigmap.common.email_trace.EmailTrace;
import com.sigmap.common.email_trace.EmailTraceRepository;
import com.sigmap.common.email_trace.EmailTraceService;
import com.sigmap.common.email_trace.TypeEmail;
import com.sigmap.common.email_trace.exeptions.EmailTraceNotFound;
import com.sigmap.common.email_trace.request.EmailTraceQuery;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.dto.UserResquestDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.mail.MailService;
import com.sigmap.common.security.Authentication;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/email-trace")
@Tag(name = "email-trace", description = "Email Trace API")
public class EmailTraceController {

    private final EmailTraceService emailTraceService;
    private final EmailTraceRepository emailTraceRepository;
    private final MailService mailService;
    private final KeycloakAdminService keycloakAdminService;



    public String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12);

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<Page<EmailTrace>> findAll(EmailTraceQuery query, Pageable pageable) {
        log.debug("API for get All EmailTrace .... ");
        Page<EmailTrace> emailTraces = emailTraceService.findAll(query.toSpecification(), pageable);
        return ResponseEntity.ok(emailTraces);
    }


    @PostMapping("/{id}/resend-email")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<Void> sendEmailManually (@PathVariable Integer id) throws MessagingException, EmailTraceNotFound {
        log.debug("API for sending email by user for email trace id: {}", id);

        // Get the current Admin ID
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        String adminId = currentUserId.orElse("System");

        // Get the email trace by id
        EmailTrace emailTrace = emailTraceRepository.findById(id)
                .orElseThrow(() -> new EmailTraceNotFound("Email trace not found with id: {}", id));

        // Check if email was already sent successfully
        if (emailTrace.isSuccessSend()) {
            log.warn("Email has already been sent successfully for trace id: {}", id);
            throw new MessagingException("Email has already been sent successfully");
        }else  {
            if (emailTrace.getType_email() == TypeEmail.REUNION_INVITATION) {
            // Send the email using the generic email service
            mailService.sendGenericEmail(
                emailTrace.getDestinataire(),
                "SIGMAP - Invitation à la réunion",
                emailTrace.getContenu(),
                adminId,
                emailTrace.getType_email()
            );

            }
            else if (emailTrace.getType_email() == TypeEmail.ACTIVATION_COMPTE) {
                mailService.sendGenericEmail(
                        emailTrace.getDestinataire(),
                        "SIGMAP - Activation Compte",
                        emailTrace.getContenu(),
                        adminId,
                        emailTrace.getType_email()
                );
            }
            else if (emailTrace.getType_email() == TypeEmail.MODIFICATION_MOT_DE_PASSE) {
                mailService.sendGenericEmail(
                        emailTrace.getDestinataire(),
                        "SIGMAP - Modification Mot de Passe",
                        emailTrace.getContenu(),
                        adminId,
                        emailTrace.getType_email()
                );
            }
            else if (emailTrace.getType_email() == TypeEmail.VALIDATION_AGPM) {
                mailService.sendGenericEmail(
                        emailTrace.getDestinataire(),
                        "SIGMAP - Validation AGPM",
                        emailTrace.getContenu(),
                        adminId,
                        emailTrace.getType_email()
                );
            }
            else if (emailTrace.getType_email() == TypeEmail.VALIDATION_PAA) {
                mailService.sendGenericEmail(
                        emailTrace.getDestinataire(),
                        "SIGMAP - Validation PAA",
                        emailTrace.getContenu(),
                        adminId,
                        emailTrace.getType_email()
                );
            }
            else if (emailTrace.getType_email() == TypeEmail.VALIDATION_PPM) {
                mailService.sendGenericEmail(
                        emailTrace.getDestinataire(),
                        "SIGMAP - Validation PPM",
                        emailTrace.getContenu(),
                        adminId,
                        emailTrace.getType_email()
                );
            }
            else if (emailTrace.getType_email() == TypeEmail.VALIDATION_DAC) {
                mailService.sendGenericEmail(
                        emailTrace.getDestinataire(),
                        "SIGMAP - Validation DAC",
                        emailTrace.getContenu(),
                        adminId,
                        emailTrace.getType_email()
                );
            }

            log.info("Email sent successfully to {} for trace id: {}", emailTrace.getDestinataire(), id);
            return ResponseEntity.ok().build();

        }
    }

}
