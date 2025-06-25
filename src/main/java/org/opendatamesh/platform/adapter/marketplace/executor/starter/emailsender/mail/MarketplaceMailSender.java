package org.opendatamesh.platform.adapter.marketplace.executor.starter.emailsender.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Interface for sending emails in the marketplace context.
 */
public interface MarketplaceMailSender {
    
    /**
     * Send a simple email message.
     *
     * @param to recipient email address
     * @param subject email subject
     * @param text email body text
     * @throws MessagingException if there is an error sending the email
     */
    void send(String to, String subject, String text) throws MessagingException;

    /**
     * Send a MIME message.
     *
     * @param message the MIME message to send
     * @throws MessagingException if there is an error sending the email
     */
    void send(MimeMessage message) throws MessagingException;

    /**
     * Create a new MIME message.
     *
     * @return a new MIME message instance
     */
    MimeMessage createMimeMessage();
} 