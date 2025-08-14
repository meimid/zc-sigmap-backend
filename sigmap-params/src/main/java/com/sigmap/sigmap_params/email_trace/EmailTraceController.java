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
    public ResponseEntity<Void> sendEmailManually (@PathVariable Integer id ) throws MessagingException, EmailTraceNotFound {

        // Get the current Admin ID
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        String AdminId = currentUserId.orElse("System");

        // Generate a temporary password
        String tmpPassword = generateTemporaryPassword();

        // get the email trace by id
        EmailTrace emailTrace = emailTraceRepository.findById(id).orElseThrow(() -> new EmailTraceNotFound("Email trace not found with id {} ", id));

        // get user info by email destinataire trace that return a UserResquestDTO
        UserResquestDTO UserInfoByEmail = keycloakAdminService.getUserByEmail(emailTrace.getDestinataire());

        // get user info by email destinataire trace that return a UserDTO
        UserDTO UserInfo = keycloakAdminService.getOneUserByEmail(emailTrace.getDestinataire());

        // tester si l'email est successSend
        if (emailTrace.isSuccessSend()) {
            throw new MessagingException("Email has already been sent");
        }else {
            if (UserInfoByEmail != null) {

                // Activation compte
                if (emailTrace.getType_email() == TypeEmail.ACTIVATION_COMPTE) {
                    mailService.RetrySendActivationEmail(UserInfoByEmail, tmpPassword, AdminId);
                }

                // Modification mot de passe
                if (emailTrace.getType_email() == TypeEmail.MODIFICATION_MOT_DE_PASSE) {
                    mailService.RetrySendEditPasswordForAdminEmail(UserInfo, tmpPassword, AdminId);
                }

            }
        }

        return ResponseEntity.ok().build();
    }


}
