package com.sigmap.common.mail;

import com.sigmap.common.keycloakAdmin.dto.UserResquestDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.io.InputStream;
import java.util.Base64;

@Service
@Slf4j
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;


    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.base-url}")
    private String getBaseUrl;


   // @Async
    public void sendActivationEmail(UserResquestDTO user, String GenericPassword) throws MessagingException {

        try {

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            InputStream logo = getClass().getClassLoader().getResourceAsStream("logo.png");

           // assert logo != null;
            byte[] sourceLogo = IOUtils.toByteArray(logo);

            String encodedStringLogo = Base64.getEncoder().encodeToString(sourceLogo);
            encodedStringLogo = "data:image/png;base64,".concat(encodedStringLogo);

            // Set email details
            helper.setTo(user.getEmail());
            helper.setSubject("Activate Your Account");
            helper.setFrom(fromEmail);

            // Prepare Thymeleaf context
            Context context = new Context();
            context.setVariable("name", user.getFirstName()+" "+user.getLastName());
            context.setVariable("password", GenericPassword);
            context.setVariable("email", user.getEmail());
            context.setVariable("baseUrl", getBaseUrl);
            context.setVariable("logo", encodedStringLogo);
            log.info("....................... {} ", encodedStringLogo);

            // Render HTML template
            String htmlContent = templateEngine.process("email/activationEmail", context);
            helper.setText(htmlContent, true); // true = isHTML

            // Send email
            javaMailSender.send(mimeMessage);

            log.info("Email sent successfully --------- {} ", user.getEmail());

        } catch (Exception e ) {
            throw new MessagingException("Failed to send email",e);
        }

    }
}
