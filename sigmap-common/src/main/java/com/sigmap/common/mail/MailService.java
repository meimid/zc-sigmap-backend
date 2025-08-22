package com.sigmap.common.mail;

import com.sigmap.common.Participant.Participant;
import com.sigmap.common.email_trace.EmailTraceService;
import com.sigmap.common.email_trace.TypeEmail;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.dto.UserResquestDTO;
import com.sigmap.common.reunion.Reunion;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;

@Configuration
@Slf4j
@Async
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;


    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.base-url}")
    private String getBaseUrl;

    @Autowired
    private EmailTraceService emailService;



    public void sendActivationEmail(UserResquestDTO user, String GenericPassword,String adminId) throws MessagingException, IOException {

        try {

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set email details
            helper.setTo(user.getEmail());
            helper.setSubject("SIGMAP – Lien d'activation compte");
            helper.setFrom(fromEmail);

            helper.setText(activationEmailContext( user, GenericPassword), true); // true = isHTML

            // Send email
            javaMailSender.send(mimeMessage);

            log.info("Email sent successfully to : {} ", user.getEmail());

            // Save email trace record
            emailService.saveEmailTrace(adminId,TypeEmail.ACTIVATION_COMPTE,"Activate Account",activationEmailContext( user, GenericPassword),user.getEmail(),true, LocalDateTime.now());

        } catch (Exception e ) {
            // Save email trace record
            emailService.saveEmailTrace(adminId,TypeEmail.ACTIVATION_COMPTE,"Activate Account",activationEmailContext( user, GenericPassword),user.getEmail(),false, LocalDateTime.now());
            throw new MessagingException("Failed to send email",e);
        }

    }
    private String activationEmailContext(UserResquestDTO user, String GenericPassword) throws IOException {
        // Load and encode logo
        InputStream logo = getClass().getClassLoader().getResourceAsStream("logo.png");
        byte[] sourceLogo = IOUtils.toByteArray(logo);
        String encodedStringLogo = Base64.getEncoder().encodeToString(sourceLogo);
        encodedStringLogo = "data:image/png;base64,".concat(encodedStringLogo);

        // Prepare Thymeleaf context
        Context context = new Context();
        context.setVariable("name", user.getFirstName()+" "+user.getLastName());
        context.setVariable("password", GenericPassword);
        context.setVariable("email", user.getEmail());
        context.setVariable("baseUrl", getBaseUrl);
        context.setVariable("logo", encodedStringLogo);
        context.setVariable("from", fromEmail);
        //log.info("....................... {} ", encodedStringLogo);

        return templateEngine.process("email/activationEmail", context);

    }



    public void sendEditPasswordForAdminEmail(UserDTO user, String GenericPassword, String adminId) throws MessagingException, IOException {

        try {

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set email details
            helper.setTo(user.getEmail());
            helper.setSubject("SIGMAP – Lien de réinitialisation de mot de passe");
            helper.setFrom(fromEmail);

            helper.setText(editPasswordForAdminEmailContext( user, GenericPassword), true); // true = isHTML

            // Send email
            javaMailSender.send(mimeMessage);

            // Save email trace record
            emailService.saveEmailTrace(adminId,TypeEmail.MODIFICATION_MOT_DE_PASSE,"Reset Password",editPasswordForAdminEmailContext( user, GenericPassword),user.getEmail(),true, LocalDateTime.now());

            //log.info("Email sent successfully to : {} ", user.getEmail());

        } catch (Exception e ) {
            emailService.saveEmailTrace(adminId,TypeEmail.MODIFICATION_MOT_DE_PASSE,"Reset Password",editPasswordForAdminEmailContext( user, GenericPassword),user.getEmail(),false, LocalDateTime.now());
            throw new MessagingException("Failed to send email",e);
        }

    }

    private String editPasswordForAdminEmailContext(UserDTO user, String GenericPassword) throws IOException {
        // Load and encode logo
        InputStream logo = getClass().getClassLoader().getResourceAsStream("logo.png");
        byte[] sourceLogo = IOUtils.toByteArray(logo);
        String encodedStringLogo = Base64.getEncoder().encodeToString(sourceLogo);
        encodedStringLogo = "data:image/png;base64,".concat(encodedStringLogo);

        // Prepare Thymeleaf context
        Context context = new Context();
        context.setVariable("name", user.getFirstName()+" "+user.getLastName());
        context.setVariable("password", GenericPassword);
        context.setVariable("email", user.getEmail());
        context.setVariable("baseUrl", getBaseUrl);
        context.setVariable("logo", encodedStringLogo);
        context.setVariable("from", fromEmail);

        // Render HTML template
        return templateEngine.process("email/resetPasswordEmail.html", context);

    }




    public void sendReunionInvitationEmail(Reunion reunion, Participant participant, String adminId) throws MessagingException, IOException {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Load and encode logo
            /*InputStream logo = getClass().getClassLoader().getResourceAsStream("logo.png");
            byte[] sourceLogo = IOUtils.toByteArray(logo);
            String encodedStringLogo = Base64.getEncoder().encodeToString(sourceLogo);
            encodedStringLogo = "data:image/png;base64,".concat(encodedStringLogo);*/

            // Set email details
            helper.setTo(participant.getMail());
            helper.setSubject("SIGMAP - Invitation à la réunion");
            helper.setFrom(fromEmail);

            // Prepare Thymeleaf context
            /*Context context = new Context();
            context.setVariable("name", participant.getPrenom() + " " + participant.getNom());
            context.setVariable("organisme", participant.getOrganisme());
            context.setVariable("fonction", participant.getFonction());
            context.setVariable("reunionDate", reunion.getReunionDate().getTime());
            context.setVariable("emplacement", reunion.getEmplacement());
            context.setVariable("object", reunion.getObject());
            context.setVariable("logo", encodedStringLogo);
            context.setVariable("from", fromEmail);*/

            //log.info("....................... {} ", encodedStringLogo);

            // Render HTML template
           // String htmlContent = templateEngine.process("email/reunionInvitationEmail", context);
            helper.setText(reunionInvitationEmailContext(reunion, participant), true);

            // Send email
            javaMailSender.send(mimeMessage);

            // Save email trace
            emailService.saveEmailTrace(adminId,TypeEmail.REUNION_INVITATION,reunion.getObject(), reunionInvitationEmailContext(reunion, participant),participant.getMail(),true,LocalDateTime.now());

            log.debug("Email sent successfully to : {} ", participant.getMail());
        } catch (Exception e) {
            emailService.saveEmailTrace(adminId,TypeEmail.REUNION_INVITATION, reunion.getObject() ,reunionInvitationEmailContext(reunion, participant),participant.getMail(),false,LocalDateTime.now());
            throw new MessagingException("Failed to send reunion invitation email", e);
        }
    }

    private String reunionInvitationEmailContext(Reunion reunion, Participant participant) throws MessagingException, IOException {
        // Load and encode logo
        InputStream logo = getClass().getClassLoader().getResourceAsStream("logo.png");
        byte[] sourceLogo = IOUtils.toByteArray(logo);
        String encodedStringLogo = Base64.getEncoder().encodeToString(sourceLogo);
        encodedStringLogo = "data:image/png;base64,".concat(encodedStringLogo);

        // Prepare Thymeleaf context
        Context context = new Context();
        context.setVariable("name", participant.getPrenom() + " " + participant.getNom());
        context.setVariable("organisme", participant.getOrganisme());
        context.setVariable("fonction", participant.getFonction());
        context.setVariable("reunionDate", reunion.getReunionDate().getTime());
        context.setVariable("emplacement", reunion.getEmplacement());
        context.setVariable("object", reunion.getObject());
        context.setVariable("logo", encodedStringLogo);
        context.setVariable("from", fromEmail);

        log.info("....................... {} ", encodedStringLogo);

        // Render HTML template
        String htmlContent = templateEngine.process("email/reunionInvitationEmail", context);
        return htmlContent;
    }


    public void sendGenericEmail(String to, String subject, String content, String adminId, TypeEmail typeEmail) throws MessagingException {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set email details
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(fromEmail);
            helper.setText(content, true); // true = isHTML

            // Send email
            javaMailSender.send(mimeMessage);

            log.info("Generic email sent successfully to : {} ", to);

            // Save email trace record
            emailService.saveEmailTrace(adminId, typeEmail, subject, content, to, true, LocalDateTime.now());

        } catch (Exception e) {
            // Save email trace record for failed attempt
            emailService.saveEmailTrace(adminId, typeEmail, subject, content, to, false, LocalDateTime.now());
            log.error("Failed to send generic email to : {} ", to, e);
            throw new MessagingException("Failed to send generic email", e);
        }
    }

}
