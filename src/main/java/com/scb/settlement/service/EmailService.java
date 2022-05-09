package com.scb.settlement.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Service("emailService")
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
public class EmailService {

    @Value("${spring.mail.fromemail}")
    private String formEmail;

    @Value("${spring.mail.toemail}")
    private String toEmail;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendMailWithAttachment(String fileName, byte[] fileToAttach, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        log.info("Trying to send mail with attachment with File name {}",fileName);
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(formEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(text);
        helper.addAttachment(fileName, new ByteArrayResource(fileToAttach));
        mailSender.send(message);
    }

    @Async("asyncExecutor")
    public void sendTemplateMail(String templateName, String subject, Map<String, Object> variables) {
        sendTemplateMail(templateName, subject, variables, Locale.getDefault());
    }

    public void sendTemplateMail(String templateName, String subject, Map<String, Object> variables, Locale locale) {
        try {
            Context context = new Context(locale);

            if (!CollectionUtils.isEmpty(variables)) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    context.setVariable(entry.getKey(), entry.getValue());
                }
            }

            final String htmlContent = templateEngine.process(templateName, context);
            sendMail(subject, htmlContent, Boolean.TRUE);
        } catch (Exception e) {
            log.error("Exception occurred while sending email", e);
        }
    }

    public void sendMail(String subject, String body) {
        sendMail(subject, body, Boolean.FALSE);
    }
    public void sendMail(String subject, String body, boolean isHtml) {
        log.info("Trying to send mail");
        try {
            final MimeMessage mimeMessage = mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            message.setSubject(subject);
            message.setFrom(formEmail);
            message.setTo(toEmail);
            message.setText(body, isHtml);
            mailSender.send(mimeMessage);
            log.info("Mail sent successfully.");
        } catch (MessagingException e) {
            log.error("Exception occurred while sending email", e);
        }
    }
}

