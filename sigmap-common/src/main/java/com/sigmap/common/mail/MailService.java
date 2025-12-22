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
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
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
        InputStream logo = getClass().getClassLoader().getResourceAsStream("logo-dark.png");
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
        InputStream logo = getClass().getClassLoader().getResourceAsStream("logo-dark.png");
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

            // Set email details
            helper.setTo(participant.getMail());
            helper.setSubject("SIGMAP - Invitation à la réunion");
            helper.setFrom(fromEmail);

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
        InputStream logo = getClass().getClassLoader().getResourceAsStream("logo-dark.png");
        byte[] sourceLogo = IOUtils.toByteArray(logo);
        String encodedStringLogo = Base64.getEncoder().encodeToString(sourceLogo);
        encodedStringLogo = "data:image/png;base64,".concat(encodedStringLogo);


        // Sanitize HTML to preserve formatting and allow additional attributes
        Safelist safelist = Safelist.relaxed()
                .addTags("h1", "h2", "h3", "h4", "h5", "h6", "p", "ol", "ul", "li", "span") // Explicitly allow necessary tags
                .addAttributes("span", "style", "class", "id", "title")
                .addAttributes("h1", "style", "class", "id", "title")
                .addAttributes("h2", "style", "class", "id", "title")
                .addAttributes("h3", "style", "class", "id", "title")
                .addAttributes("h4", "style", "class", "id", "title")
                .addAttributes("h5", "style", "class", "id", "title")
                .addAttributes("h6", "style", "class", "id", "title")
                .addAttributes("p", "style", "class", "id", "title")
                .addAttributes("li", "style", "class", "id", "title")
                .addProtocols("span", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("h1", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("h2", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("h3", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("h4", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("h5", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("h6", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("p", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("li", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration");
        String safeHtml = Jsoup.clean(reunion.getObject(), safelist);

        // Prepare Thymeleaf context
        Context context = new Context();
        context.setVariable("name", participant.getPrenom() + " " + participant.getNom());
        context.setVariable("organisme", participant.getOrganisme());
        context.setVariable("fonction", participant.getFonction());
        context.setVariable("reunionDate", reunion.getReunionDate().getTime());
        context.setVariable("emplacement", reunion.getEmplacement());
        context.setVariable("object", safeHtml); // Use cleaned text
        context.setVariable("logo", encodedStringLogo);
        context.setVariable("from", fromEmail);

       // log.info("....................... {} ", encodedStringLogo);

        // Render HTML template
        return templateEngine.process("email/reunionInvitationEmail", context);
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

    public void sendReunionAnnulerEmail(Reunion reunion, Participant participant, String adminId) throws MessagingException, IOException {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set email details
            helper.setTo(participant.getMail());
            helper.setSubject("SIGMAP - Annulation du réunion");
            helper.setFrom(fromEmail);

            // Render HTML template
            // String htmlContent = templateEngine.process("email/reunionInvitationEmail", context);
            helper.setText(reunionInvitationAnnulerEmailContext(reunion, participant), true);

            // Send email
            javaMailSender.send(mimeMessage);

            // Save email trace
            emailService.saveEmailTrace(adminId,TypeEmail.REUNION_ANNULER,reunion.getObject(), reunionInvitationAnnulerEmailContext(reunion, participant),participant.getMail(),true,LocalDateTime.now());

            log.debug("Email sent successfully to : {} ", participant.getMail());
        } catch (Exception e) {
            emailService.saveEmailTrace(adminId,TypeEmail.REUNION_ANNULER, reunion.getObject() ,reunionInvitationAnnulerEmailContext(reunion, participant),participant.getMail(),false,LocalDateTime.now());
            throw new MessagingException("Failed to send reunion annuler email", e);
        }
    }

    private String reunionInvitationAnnulerEmailContext(Reunion reunion, Participant participant) throws MessagingException, IOException {
        // Load and encode logo
        InputStream logo = getClass().getClassLoader().getResourceAsStream("logo-dark.png");
        byte[] sourceLogo = IOUtils.toByteArray(logo);
        String encodedStringLogo = Base64.getEncoder().encodeToString(sourceLogo);
        encodedStringLogo = "data:image/png;base64,".concat(encodedStringLogo);


        // Sanitize HTML to preserve formatting and allow additional attributes
        Safelist safelist = Safelist.relaxed()
                .addTags("h1", "h2", "h3", "h4", "h5", "h6", "p", "ol", "ul", "li", "span") // Explicitly allow necessary tags
                .addAttributes("span", "style", "class", "id", "title")
                .addAttributes("h1", "style", "class", "id", "title")
                .addAttributes("h2", "style", "class", "id", "title")
                .addAttributes("h3", "style", "class", "id", "title")
                .addAttributes("h4", "style", "class", "id", "title")
                .addAttributes("h5", "style", "class", "id", "title")
                .addAttributes("h6", "style", "class", "id", "title")
                .addAttributes("p", "style", "class", "id", "title")
                .addAttributes("li", "style", "class", "id", "title")
                .addProtocols("span", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("h1", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("h2", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("h3", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("h4", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("h5", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("h6", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("p", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration")
                .addProtocols("li", "style", "color", "font-size", "font-weight", "font-family", "text-align", "margin", "padding", "text-decoration");
        String safeHtml = Jsoup.clean(reunion.getObject(), safelist);

        // Prepare Thymeleaf context
        Context context = new Context();
        context.setVariable("name", participant.getPrenom() + " " + participant.getNom());
        context.setVariable("organisme", participant.getOrganisme());
        context.setVariable("fonction", participant.getFonction());
        context.setVariable("reunionDate", reunion.getReunionDate().getTime());
        context.setVariable("emplacement", reunion.getEmplacement());
        context.setVariable("object", safeHtml); // Use cleaned text
        context.setVariable("logo", encodedStringLogo);
        context.setVariable("from", fromEmail);

        // log.info("....................... {} ", encodedStringLogo);

        // Render HTML template
        return templateEngine.process("email/reunionAnnulationEmail", context);
    }

    /**
     * Construit le contenu HTML de l'email de notification de décision de commission.
     *
     * Exemple de contenu :
     * Bonjour,
     * Le dossier "nom du dossier" transféré à la commission "nom de la commission"
     * a été traité lors de la réunion "date réunion".
     * Décision: "Décision réunion", motif "xxxxx" (si réserves) si renseigné.
     * Cordialement,
     */
    private String commissionDecisionEmailContext(String dossierName,
                                                  String commissionName,
                                                  String reunionDateLabel,
                                                  String decisionLabel,
                                                  String motif) throws IOException {

        // 1. Charger et encoder le logo
        InputStream logo = getClass().getClassLoader().getResourceAsStream("logo-dark.png");
        byte[] sourceLogo = IOUtils.toByteArray(logo);
        String encodedStringLogo = Base64.getEncoder().encodeToString(sourceLogo);
        encodedStringLogo = "data:image/png;base64,".concat(encodedStringLogo);

        // 2. Préparer le contexte Thymeleaf
        Context context = new Context();
        context.setVariable("dossierName", dossierName);
        context.setVariable("commissionName", commissionName);
        context.setVariable("reunionDate", reunionDateLabel);
        context.setVariable("decision", decisionLabel);
        context.setVariable("motif", motif);
        context.setVariable("logo", encodedStringLogo);
        context.setVariable("from", fromEmail);

        // 3. Générer le HTML depuis le template
        return templateEngine.process("email/commissionDecisionNotificationEmail", context);
    }

    /**
     * Envoie un email de notification de décision de commission
     * (utilisable pour CPMP -> AC et CNCMP -> CPMP).
     *
     * @param toEmail           Destinataire (AC, CPMP, etc.)
     * @param dossierName       Nom du dossier
     * @param commissionName    Nom de la commission (ex: "CPMP", "CNCMP")
     * @param reunionDateLabel  Date de la réunion sous forme de String déjà formatée
     * @param decisionLabel     Libellé de la décision
     * @param motif             Motif si réserves (peut être null ou vide)
     * @param adminId           Id de l'utilisateur qui déclenche l'envoi (pour EmailTrace)
     * @param typeEmail         TypeEmail dédié (à ajouter dans ton enum)
     */
    public void sendCommissionDecisionNotificationEmail(
            String toEmail,
            String dossierName,
            String commissionName,
            String reunionDateLabel,
            String decisionLabel,
            String motif,
            String adminId,
            TypeEmail typeEmail
    ) {


        if (toEmail == null || toEmail.isBlank()) {
            log.warn("❌ Email destinataire NULL → email non envoyé (type={})", typeEmail);

            // On trace quand même en base, mais avec success=false
            try {
                emailService.saveEmailTrace(
                        adminId != null ? adminId : "SYSTEM",
                        typeEmail != null ? typeEmail : TypeEmail.VALIDATION_PPM,
                        "[DESTINATAIRE NULL] Commission " + (commissionName != null ? commissionName : "N/A"),
                        "",
                        "",
                        false,
                        LocalDateTime.now()
                );
            } catch (Exception ex) {
                log.error("❌ Impossible d'enregistrer la trace email destinataire null", ex);
            }

            return;
        }

        dossierName = dossierName != null ? dossierName : "N/A";
        commissionName = commissionName != null ? commissionName : "Commission";
        reunionDateLabel = reunionDateLabel != null ? reunionDateLabel : "N/A";
        decisionLabel = decisionLabel != null ? decisionLabel : "Non renseignée";
        motif = motif != null ? motif : "";

        String subject = "SIGMAP – Décision de la commission " + commissionName;

        String content; // Préparation variable

        try {
            // HTML template sécurisé
            content = commissionDecisionEmailContext(
                    dossierName,
                    commissionName,
                    reunionDateLabel,
                    decisionLabel,
                    motif
            );
        } catch (Exception e) {
            log.error("⚠ Impossible de générer le contenu HTML, fallback en texte simple.");
            content = "<p>Décision de commission</p>";
        }

        boolean success = false;

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom(fromEmail != null ? fromEmail : "no-reply@sigmap.tn");
            helper.setText(content, true);

            javaMailSender.send(message);
            success = true;

            log.info("✔ Email commission envoyé à {}", toEmail);

        } catch (Exception e) {
            success = false;
            log.error("❌ Envoi email commission échoué pour {}", toEmail, e);
        }

        try {
            emailService.saveEmailTrace(
                    adminId != null ? adminId : "SYSTEM",
                    typeEmail != null ? typeEmail : TypeEmail.VALIDATION_PPM,
                    subject,
                    content,
                    toEmail,
                    success,
                    LocalDateTime.now()
            );

        } catch (Exception e) {
            log.error("❌ Impossible de sauvegarder email trace", e);
        }
    }

    private String dacShortlistInvitationEmailContext(
            String acName,
            String aoObjet,
            String aoReference,
            String dateLimiteSoumission
    ) throws IOException {

        InputStream logo = getClass().getClassLoader().getResourceAsStream("logo-dark.png");
        byte[] sourceLogo = (logo != null) ? IOUtils.toByteArray(logo) : new byte[0];
        String encodedStringLogo = Base64.getEncoder().encodeToString(sourceLogo);
        encodedStringLogo = "data:image/png;base64,".concat(encodedStringLogo);

        Context context = new Context();
        context.setVariable("acName", acName);
        context.setVariable("aoObjet", aoObjet);
        context.setVariable("aoReference", aoReference);
        context.setVariable("dateLimiteSoumission", dateLimiteSoumission);
        context.setVariable("logo", encodedStringLogo);
        context.setVariable("from", fromEmail);

        return templateEngine.process("email/dacShortlistInvitationEmail", context);
    }



    public void sendDacShortlistInvitationEmail(
            String toEmail,
            String acName,
            String aoObjet,
            String aoReference,
            String dateLimiteSoumission,
            String adminId,
            TypeEmail typeEmail
    ) throws MessagingException {

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("❌ DAC shortlist email: destinataire NULL → email non envoyé");
            try {
                emailService.saveEmailTrace(
                        adminId != null ? adminId : "SYSTEM",
                        typeEmail != null ? typeEmail : TypeEmail.VALIDATION_PPM,
                        "[DESTINATAIRE NULL] DAC Shortlist",
                        "",
                        "",
                        false,
                        LocalDateTime.now()
                );
            } catch (Exception ex) {
                log.error("❌ Impossible d'enregistrer EmailTrace (dest null)", ex);
            }
            return;
        }

        acName = (acName != null && !acName.isBlank()) ? acName : "L'AC";
        aoObjet = (aoObjet != null && !aoObjet.isBlank()) ? aoObjet : "Objet AO";
        aoReference = (aoReference != null && !aoReference.isBlank()) ? aoReference : "Référence AO";
        dateLimiteSoumission = (dateLimiteSoumission != null && !dateLimiteSoumission.isBlank())
                ? dateLimiteSoumission
                : "N/A";

        String subject = "SIGMAP – Invitation à soumissionner – " + aoReference;

        String content;
        try {
            content = dacShortlistInvitationEmailContext(
                    acName,
                    aoObjet,
                    aoReference,
                    dateLimiteSoumission
            );
        } catch (Exception e) {
            log.error("⚠ DAC shortlist email: génération HTML échouée → fallback", e);

            content =
                    "<p>Mesdames, Messieurs,</p>" +
                            "<p><b>" + acName + "</b> vous informe que vous avez été initialement sélectionné pour remettre votre offre/Proposition pour " +
                            "<b>" + aoObjet + "</b>, Référence <b>" + aoReference + "</b>, selon les dispositions définies dans le dossier d'appel à concurrence " +
                            "avec, comme date limite de soumission le <b>" + dateLimiteSoumission + "</b>.</p>";
        }

        boolean success = false;

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom(fromEmail != null ? fromEmail : "no-reply@sigmap.tn");
            helper.setText(content, true);

            javaMailSender.send(mimeMessage);
            success = true;

            log.info("✔ DAC shortlist invitation email sent to {}", toEmail);

        } catch (Exception e) {
            success = false;
            log.error("❌ Failed to send DAC shortlist invitation email to {}", toEmail, e);
            throw new MessagingException("Failed to send DAC shortlist invitation email", e);

        } finally {
            try {
                emailService.saveEmailTrace(
                        adminId != null ? adminId : "SYSTEM",
                        typeEmail != null ? typeEmail : TypeEmail.VALIDATION_PPM,
                        subject,
                        content,
                        toEmail,
                        success,
                        LocalDateTime.now()
                );
            } catch (Exception e) {
                log.error("❌ Impossible de sauvegarder EmailTrace (DAC shortlist)", e);
            }
        }
    }





}
