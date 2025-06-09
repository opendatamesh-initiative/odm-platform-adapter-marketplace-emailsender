package org.opendatamesh.platform.adapter.marketplace.executor.starter.service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.opendatamesh.platform.adapter.marketplace.executor.starter.resources.MarketplaceRequestRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final MustacheFactory mustacheFactory;
    private final String senderEmail;
    private final String subscribeTemplatePath;
    private final String unsubscribeTemplatePath;

    @Autowired
    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.from}") String senderEmail, @Value("${odm.email.subscribe-template-path}") String subscribeTemplatePath, @Value("${odm.email.unsubscribe-template-path}") String unsubscribeTemplatePath) {
        this.mailSender = mailSender;
        this.mustacheFactory = new DefaultMustacheFactory();
        this.senderEmail = senderEmail;
        this.subscribeTemplatePath = subscribeTemplatePath;
        this.unsubscribeTemplatePath = unsubscribeTemplatePath;
    }

    private String loadTemplate(String templatePath) {
        if (templatePath != null && !templatePath.isEmpty()) {
            try {
                if (Files.exists(Paths.get(templatePath))) {
                    return new String(Files.readAllBytes(Paths.get(templatePath)), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                log.warn("Could not load template from file '{}': {}", templatePath, e.getMessage());
            }
        }
        try {
            Resource resource = new org.springframework.core.io.DefaultResourceLoader().getResource(templatePath);
            if (resource.exists()) {
                return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("Could not load default template from classpath: {}", e.getMessage());
        }
        return "Hello {{requesterIdentifier}},\n\nYour request for access to the following data product has been processed:\n\nData Product: {{dataProductFqn}}\nAccess Period: {{startDate}} to {{endDate}}\nConsumer: {{consumerIdentifier}}\n\nBest regards,\nODM Platform Team";
    }

    public void sendAccessEmail(MarketplaceRequestRes request) {
        try {
            String template = loadTemplate(subscribeTemplatePath);
            Mustache mustache = mustacheFactory.compile(new StringReader(template), "subscribe-email-template");

            Map<String, Object> context = new HashMap<>();
            context.put("data", request);

            StringWriter writer = new StringWriter();
            mustache.execute(writer, context);
            String emailContent = writer.toString();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(senderEmail);
            helper.setTo(request.getRequest().getRequester().getIdentifier());
            helper.setSubject("Access Granted: " + request.getRequest().getProvider().getDataProductFqn());
            helper.setText(emailContent, false);

            mailSender.send(message);
            log.info("Access email sent successfully to {}", request.getRequest().getRequester().getIdentifier());
        } catch (MessagingException e) {
            log.error("Failed to send access email", e);
            throw new RuntimeException("Failed to send access email", e);
        }
    }

    public void sendUnsubscribeEmail(MarketplaceRequestRes request) {
        try {
            String template = loadTemplate(unsubscribeTemplatePath);
            Mustache mustache = mustacheFactory.compile(new StringReader(template), "unsubscribe-email-template");

            Map<String, Object> context = new HashMap<>();
            context.put("data", request);

            StringWriter writer = new StringWriter();
            mustache.execute(writer, context);
            String emailContent = writer.toString();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(senderEmail);
            helper.setTo(request.getRequest().getRequester().getIdentifier());
            helper.setSubject("Unsubscription Confirmed: " + request.getRequest().getProvider().getDataProductFqn());
            helper.setText(emailContent, false);

            mailSender.send(message);
            log.info("Unsubscribe email sent successfully to {}", request.getRequest().getRequester().getIdentifier());
        } catch (MessagingException e) {
            log.error("Failed to send unsubscribe email", e);
            throw new RuntimeException("Failed to send unsubscribe email", e);
        }
    }
} 