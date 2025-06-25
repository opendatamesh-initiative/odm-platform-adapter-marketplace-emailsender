package org.opendatamesh.platform.adapter.marketplace.executor.starter.emailsender.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Adapter implementation that uses Spring's JavaMailSender to send emails.
 */
@Component
public class JavaMailSenderAdapter implements MarketplaceMailSender {

    private final JavaMailSender mailSender;

    @Autowired
    public JavaMailSenderAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, String subject, String text) throws MessagingException {
        MimeMessage message = createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);
        send(message);
    }

    @Override
    public void send(MimeMessage message) throws MessagingException {
        mailSender.send(message);
    }

    @Override
    public MimeMessage createMimeMessage() {
        return mailSender.createMimeMessage();
    }
} 