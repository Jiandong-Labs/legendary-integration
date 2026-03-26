package com.jiandong.legendaryintegration.mail;

import java.io.IOException;

import com.jiandong.legendaryintegration.mail.parser.MimeMessageParser;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.mail.dsl.Mail;

@Profile("mail")
@Configuration
@EnableConfigurationProperties(InboundMailConfig.ImapMailProperties.class)
public class InboundMailConfig {

	private static final Logger log = LoggerFactory.getLogger(InboundMailConfig.class);

	@ConfigurationProperties("mail")
	public record ImapMailProperties(String host, Integer port, String username, String password, String protocol) {

	}

	@Bean
	public IntegrationFlow imapMailFlow(ImapMailProperties mailProperties) {
		var user = mailProperties.username.replace("@", "%40");
		var url = "%s://%s:%s@%s:%d/INBOX".formatted(mailProperties.protocol, user, mailProperties.password, mailProperties.host, mailProperties.port);
		return IntegrationFlow.from(Mail.imapInboundAdapter(url)
								.shouldDeleteMessages(false)
								.shouldMarkMessagesAsRead(true)
								.autoCloseFolder(false)
								.javaMailProperties(p -> p.put("mail.debug", "false")),
						e -> e
								.autoStartup(true)
								.poller(p -> p.fixedDelay(120 * 1000))
				)
				.channel(MessageChannels.queue("imapChannel"))
				.get();
	}

	@Bean
	public IntegrationFlow mailConsumer() {
		return IntegrationFlow.from("imapChannel")
				.handle((GenericHandler<MimeMessage>) (payload, headers) -> {
					try {
						MimeMessageParser parser = new MimeMessageParser(payload);
						parser.parse();
						log.info("subject:{}, from:{}, to:{}, receive date:{}, text content:{}, html content:{} attachment:{}",
								parser.getSubject(), parser.getFrom(), parser.getTo(), payload.getReceivedDate(),
								parser.getPlainContent(), parser.getHtmlContent(), parser.getAttachmentList());
					}
					catch (MessagingException | IOException e) {
						throw new RuntimeException(e);
					}
					return null;
				})
				.get();
	}

}
