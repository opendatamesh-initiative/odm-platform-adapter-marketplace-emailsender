package org.opendatamesh.platform.adapter.marketplace.executor.starter.service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.opendatamesh.platform.adapter.marketplace.executor.starter.resources.MarketplaceRequestRes;
import org.opendatamesh.platform.adapter.marketplace.emailsender.mail.MarketplaceMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
/**
 * Service class for handling email operations in the marketplace.
 * This service is responsible for sending emails using templates for various marketplace actions
 * such as access requests and unsubscriptions.
 */

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final MarketplaceMailSender mailSender;
    private final MustacheFactory mustacheFactory;
    private final String senderEmail;
    private final String subscribeTemplatePath;
    private final String unsubscribeTemplatePath;
    private final boolean useHtml;

    @Autowired
    public EmailService(MarketplaceMailSender mailSender,
                        @Value("${odm.email.from}") String senderEmail,
                        @Value("${odm.email.subscribe-template-path}") String subscribeTemplatePath,
                        @Value("${odm.email.unsubscribe-template-path}") String unsubscribeTemplatePath,
                        @Value("${odm.email.useHtml:false}") boolean useHtml) {
        this.mailSender = mailSender;
        this.senderEmail = senderEmail;
        this.subscribeTemplatePath = subscribeTemplatePath;
        this.unsubscribeTemplatePath = unsubscribeTemplatePath;
        this.mustacheFactory = new DefaultMustacheFactory();
        this.useHtml = useHtml;
    }

    /**
     * Loads a Mustache template from a file or classpath. The template is used to send emails to the requester.
     * Templates can be configured using the following properties:
     * - odm.email.subscribe-template-path
     * - odm.email.unsubscribe-template-path
     * 
     * @param templatePath The path to the template file
     * @return The template content as a string
     * @throws IllegalArgumentException if templatePath is null
     * @throws IllegalStateException if template cannot be loaded from file or classpath
     */
    private String loadTemplate(String templatePath) {
        validateTemplatePath(templatePath);
        
        String template = tryLoadFromFileSystem(templatePath);
        if (template != null) {
            return template;
        }

        template = tryLoadFromClasspath(templatePath);
        if (template != null) {
            return template;
        }

        throw new IllegalStateException("Could not load email template from path: " + templatePath);
    }

    private void validateTemplatePath(String templatePath) {
        if (templatePath == null) {
            throw new IllegalArgumentException("Template path cannot be null");
        }
    }

    private String tryLoadFromFileSystem(String templatePath) {
        if (templatePath.isEmpty()) {
            return null;
        }

        try {
            if (Files.exists(Paths.get(templatePath))) {
                return new String(Files.readAllBytes(Paths.get(templatePath)), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("Could not load template from file '{}': {}", templatePath, e.getMessage());
        }
        return null;
    }

    private String tryLoadFromClasspath(String templatePath) {
        try {
            Resource resource = new org.springframework.core.io.DefaultResourceLoader().getResource(templatePath);
            if (resource.exists()) {
                return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("Could not load default template from classpath: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Sends an email to the requester when access is granted.
     * 
     * @param request The marketplace request containing the access details
     */
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
            helper.setText(emailContent, useHtml);

            mailSender.send(message);
            log.info("Access email sent successfully to {}", request.getRequest().getRequester().getIdentifier());
        } catch (MessagingException e) {
            log.error("Failed to send access email", e);
            throw new RuntimeException("Failed to send access email", e);
        }
    }

    /**
     * Sends an email to the requester when access is revoked.
     * 
     * @param request The marketplace request containing the access details
     */
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
            helper.setText(emailContent, useHtml);

            mailSender.send(message);
            log.info("Unsubscribe email sent successfully to {}", request.getRequest().getRequester().getIdentifier());
        } catch (MessagingException e) {
            log.error("Failed to send unsubscribe email", e);
            throw new RuntimeException("Failed to send unsubscribe email", e);
        }
    }
} 