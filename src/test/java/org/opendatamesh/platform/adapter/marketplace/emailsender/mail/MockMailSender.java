package org.opendatamesh.platform.adapter.marketplace.emailsender.mail;

import org.opendatamesh.platform.adapter.marketplace.executor.starter.emailsender.mail.MarketplaceMailSender;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Mock implementation of MarketplaceMailSender for testing purposes.
 * This class keeps track of sent messages and allows verification in tests.
 */
public class MockMailSender implements MarketplaceMailSender {
    
    private final List<MimeMessage> sentMessages = new ArrayList<>();
    private final List<SimpleMailMessage> sentSimpleMessages = new ArrayList<>();
    private final Session session;

    public MockMailSender() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");
        this.session = Session.getInstance(props);
    }

    @Override
    public void send(String to, String subject, String text) throws MessagingException {
        sentSimpleMessages.add(new SimpleMailMessage(to, subject, text));
    }

    @Override
    public void send(MimeMessage message) throws MessagingException {
        sentMessages.add(message);
    }

    @Override
    public MimeMessage createMimeMessage() {
        return new MimeMessage(session);
    }

    /**
     * Get all sent MIME messages.
     *
     * @return list of sent MIME messages
     */
    public List<MimeMessage> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    /**
     * Get all sent simple messages.
     *
     * @return list of sent simple messages
     */
    public List<SimpleMailMessage> getSentSimpleMessages() {
        return new ArrayList<>(sentSimpleMessages);
    }

    /**
     * Clear all recorded messages.
     */
    public void clear() {
        sentMessages.clear();
        sentSimpleMessages.clear();
    }

    /**
     * Simple class to hold basic email message information.
     */
    public static class SimpleMailMessage {
        private final String to;
        private final String subject;
        private final String text;

        public SimpleMailMessage(String to, String subject, String text) {
            this.to = to;
            this.subject = subject;
            this.text = text;
        }

        public String getTo() {
            return to;
        }

        public String getSubject() {
            return subject;
        }

        public String getText() {
            return text;
        }
    }
} 