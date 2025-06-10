package org.opendatamesh.platform.adapter.marketplace.executor.starter.config;

import org.opendatamesh.platform.adapter.marketplace.emailsender.mail.MarketplaceMailSender;
import org.opendatamesh.platform.adapter.marketplace.emailsender.mail.MockMailSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@EnableAutoConfiguration(exclude = {MailSenderAutoConfiguration.class})
public class TestConfig {
    
    @Bean
    @ConditionalOnMissingBean
    public MarketplaceMailSender mailSender() {
        return new MockMailSender();
    }
} 