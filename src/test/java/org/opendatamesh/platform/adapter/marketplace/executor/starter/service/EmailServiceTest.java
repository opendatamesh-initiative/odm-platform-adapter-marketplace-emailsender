package org.opendatamesh.platform.adapter.marketplace.executor.starter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.opendatamesh.platform.adapter.marketplace.emailsender.mail.MarketplaceMailSender;
import org.opendatamesh.platform.adapter.marketplace.emailsender.mail.MockMailSender;
import org.opendatamesh.platform.adapter.marketplace.executor.starter.resources.MarketplaceRequestRes;
import org.opendatamesh.platform.adapter.marketplace.executor.starter.resources.RequestRes;
import org.opendatamesh.platform.adapter.marketplace.executor.starter.resources.RequesterRes;
import org.opendatamesh.platform.adapter.marketplace.executor.starter.resources.ProviderRes;
import org.opendatamesh.platform.adapter.marketplace.executor.starter.config.TestConfig;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private MarketplaceMailSender mailSender;

    @BeforeEach
    public void resetMockMailSender() {
        if (mailSender instanceof MockMailSender) {
            ((MockMailSender) mailSender).clear();
        }
    }

    private MarketplaceRequestRes createTestRequest() {
        MarketplaceRequestRes request = new MarketplaceRequestRes();
        request.setOperation("MARKETPLACE_SUBSCRIBE");
        request.setV("1.0");
        
        RequestRes requestDetails = new RequestRes();
        requestDetails.setName("Test Request");
        requestDetails.setIdentifier("test-request-123");
        
        RequesterRes requester = new RequesterRes();
        requester.setType("user");
        requester.setIdentifier("user@example.com");
        requestDetails.setRequester(requester);
        
        ProviderRes provider = new ProviderRes();
        provider.setDataProductFqn("test/product/1.0.0");
        requestDetails.setProvider(provider);
        
        requestDetails.setStartDate(new Date());
        requestDetails.setEndDate(new Date(System.currentTimeMillis() + 86400000)); // Tomorrow
        
        request.setRequest(requestDetails);
        return request;
    }

    @Test
    public void testSendAccessEmail() throws Exception {
        // Verify we have the correct mail sender
        assertTrue(mailSender instanceof MockMailSender, "Mail sender should be an instance of MockMailSender");
        MockMailSender mockMailSender = (MockMailSender) mailSender;

        // Given
        MarketplaceRequestRes request = createTestRequest();

        // When
        emailService.sendAccessEmail(request);

        // Then
        var sentMessages = mockMailSender.getSentMessages();
        assertEquals(1, sentMessages.size());
        MimeMessage message = sentMessages.get(0);
        assertNotNull(message);
        
        // Verify email content
        String content = getTextFromMimeMessage(message);
        assertTrue(content.contains("Your request for access to the following data product has been processed:"));
        assertTrue(content.contains(request.getRequest().getProvider().getDataProductFqn()));
        assertTrue(content.contains(request.getRequest().getRequester().getIdentifier()));
    }

    @Test
    public void testSendUnsubscribeEmail() throws Exception {
        // Verify we have the correct mail sender
        assertTrue(mailSender instanceof MockMailSender, "Mail sender should be an instance of MockMailSender");
        MockMailSender mockMailSender = (MockMailSender) mailSender;

        // Given
        MarketplaceRequestRes request = createTestRequest();

        // When
        emailService.sendUnsubscribeEmail(request);

        // Then
        var sentMessages = mockMailSender.getSentMessages();
        assertEquals(1, sentMessages.size());
        MimeMessage message = sentMessages.get(0);
        assertNotNull(message);
        
        // Verify email content
        String content = getTextFromMimeMessage(message);
        assertTrue(content.contains("Your access to the following data product has been terminated:"));
        assertTrue(content.contains(request.getRequest().getProvider().getDataProductFqn()));
        assertTrue(content.contains(request.getRequest().getRequester().getIdentifier()));
    }

    private static String getTextFromMimeMessage(MimeMessage message) throws Exception {
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof MimeMultipart) {
            return getTextFromMimeMultipart((MimeMultipart) content);
        }
        return null;
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            Part part = mimeMultipart.getBodyPart(i);
            Object partContent = part.getContent();
            if (partContent instanceof String) {
                result.append((String) partContent);
            } else if (partContent instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) partContent));
            }
        }
        return result.toString();
    }
} 