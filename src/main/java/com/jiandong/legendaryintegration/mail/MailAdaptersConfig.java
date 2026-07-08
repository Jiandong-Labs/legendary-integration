package com.jiandong.legendaryintegration.mail;

import java.io.IOException;
import java.util.Objects;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.mail2.jakarta.util.MimeMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@EnableConfigurationProperties(MailAdaptersConfig.ImapMailProperties.class)
class MailAdaptersConfig {

	private static final Logger log = LoggerFactory.getLogger(MailAdaptersConfig.class);

	@Value("${spring.mail.username}")
	private String username;

	@Bean
	IntegrationFlow mailSendFlow(JavaMailSender javaMailSender) {
		return flow -> flow
				.enrichHeaders(Mail.headers()
						.from(username)
						.toFunction(message -> (String[]) message.getHeaders().get("to"))
						.subjectFunction(message -> Objects.requireNonNullElse((String) message.getHeaders().get("subject"), "No Subject")))
				.handle(Mail.outboundAdapter(javaMailSender));
	}

	@ConfigurationProperties("mail")
	record ImapMailProperties(String host, Integer port, String username, String password, String protocol) {

	}

	@Bean
	IntegrationFlow mailReceivingFlow(ImapMailProperties mailProperties) {
		var user = mailProperties.username.replace("@", "%40");
		var url = "%s://%s:%s@%s:%d/INBOX".formatted(mailProperties.protocol, user, mailProperties.password, mailProperties.host, mailProperties.port);
		return IntegrationFlow.from(Mail.imapInboundAdapter(url)
								.shouldDeleteMessages(false)
								.shouldMarkMessagesAsRead(true)
								.autoCloseFolder(false)
								.javaMailProperties(p -> p.put("mail.debug", "false")),
						e -> e
								.id("imapMailPollingAdapter")
								.autoStartup(false)
								.poller(p -> p.fixedDelay(120 * 1000))
				)
				.<MimeMessage>handle((payload, headers) -> {
					MimeMessageParser parser;
					try {
						parser = new MimeMessageParser(payload);
						parser.parse();
						log.info("subject:{}, from:{}, to:{}, receive date:{}, text content:{}, html content:{} attachment:{}",
								parser.getSubject(), parser.getFrom(), parser.getTo(), payload.getReceivedDate(),
								parser.getPlainContent(), parser.getHtmlContent(), parser.getAttachmentList());
					}
					catch (MessagingException | IOException e) {
						throw new RuntimeException(e);
					}
					return parser;
				})
				.channel("mailOutputChannel")
				.get();
	}

	@Bean
	QueueChannel mailOutputChannel() {
		return new QueueChannel();
	}

}
