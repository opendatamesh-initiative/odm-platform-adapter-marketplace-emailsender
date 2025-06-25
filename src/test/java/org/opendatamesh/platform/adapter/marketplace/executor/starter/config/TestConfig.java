package org.opendatamesh.platform.adapter.marketplace.executor.starter.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@TestConfiguration
@EnableAutoConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return new CapturingJavaMailSender();
    }
    
    /**
     * JavaMailSender that captures sent messages for testing
     */
    public static class CapturingJavaMailSender extends JavaMailSenderImpl {
        
        private final List<MimeMessage> sentMessages = new ArrayList<>();
        
        @Override
        public void send(MimeMessage... mimeMessages) {
            for (MimeMessage message : mimeMessages) {
                sentMessages.add(message);
            }
        }
        
        @Override
        public MimeMessage createMimeMessage() {
            Properties props = new Properties();
            props.put("mail.smtp.host", "localhost");
            Session session = Session.getInstance(props);
            return new MimeMessage(session);
        }
        
        @Override
        public MimeMessage createMimeMessage(java.io.InputStream contentStream) {
            return createMimeMessage();
        }
        
        public List<MimeMessage> getSentMessages() {
            return new ArrayList<>(sentMessages);
        }
        
        public void clear() {
            sentMessages.clear();
        }
    }
} 