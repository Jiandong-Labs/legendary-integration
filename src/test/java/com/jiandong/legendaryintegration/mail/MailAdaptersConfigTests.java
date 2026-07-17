package com.jiandong.legendaryintegration.mail;

import java.io.IOException;
import java.util.Properties;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simplejavamail.api.email.Email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@EnableIntegration
@SpringBootTest(classes = {MailAdaptersConfig.class})
@ImportAutoConfiguration({MailSenderAutoConfiguration.class})
@DirtiesContext
class MailAdaptersConfigTests {

	GreenMail mailServer;

	@Autowired
	MailProperties senderMailProperties;

	@Autowired
	MailAdaptersConfig.ImapMailProperties receiverMailProperties;

	private String sender;

	private String receiver;

	@BeforeEach
	void setup() {
		mailServer = new GreenMail(ServerSetupTest.SMTP_IMAP);

		sender = senderMailProperties.getUsername();
		receiver = receiverMailProperties.username();

		mailServer.setUser(sender, senderMailProperties.getPassword());
		mailServer.setUser(receiver, receiverMailProperties.password());
		mailServer.start();
	}

	@AfterEach
	void tearDown() {
		mailServer.stop();
	}

	@Autowired
	@Qualifier("mailSendFlow.input")
	MessageChannel mailSendingChannel;

	@Autowired
	@Qualifier("mailSendFlow.mail:outbound-channel-adapter#0")
	private MessageHandler mailSendingHandler;

	@Autowired
	@Qualifier("imapMailPollingAdapter")
	SourcePollingChannelAdapter imapMailPollingAdapter;

	@Autowired
	QueueChannel mailOutputChannel;

	@Test
	void happyFlow() throws MessagingException, IOException {
		assertThat(TestUtils.<String>getPropertyValue(this.mailSendingHandler, "mailSender.host")).isEqualTo("localhost");
		Properties javaMailProperties = TestUtils.getPropertyValue(this.mailSendingHandler, "mailSender.javaMailProperties");
		assertThat(javaMailProperties.getProperty("mail.debug")).isEqualTo("false");

		// sending to receiver
		mailSendingChannel.send(MessageBuilder
				.withPayload("this is a test email message.")
				.setHeader("from", sender)
				.setHeader("to", new String[] {receiver})
				.setHeader("subject", "hello xyz")
				.build());

		// check in mail server
		mailServer.waitForIncomingEmail(10000, 1);
		MimeMessage[] mimeMessages = mailServer.getReceivedMessagesForDomain("cn");
		assertThat(mimeMessages.length > 0).isTrue();
		MimeMessage message = mimeMessages[0];
		assertThat(message.getFrom()).containsOnly(new InternetAddress(sender));
		assertThat(message.getRecipients(MimeMessage.RecipientType.TO)).containsOnly(new InternetAddress(receiver));
		assertThat(message.getSubject()).isEqualTo("hello xyz");
		assertThat(message.getContent()).asString().isEqualTo("this is a test email message.");

		// receiving
		imapMailPollingAdapter.start();

		Message<?> mailMessage = mailOutputChannel.receive(10000);
		Assertions.assertThat(mailMessage)
				.extracting(Message::getPayload)
				.isInstanceOfSatisfying(Email.class, email -> {
					assertThat(email.getFromRecipient().getAddress()).isEqualTo(sender);
					assertThat(email.getToRecipients().get(0).getAddress()).isEqualTo(receiver);
					assertThat(email.getSubject()).isEqualTo("hello xyz");
					assertThat(email.getPlainText()).isEqualTo("this is a test email message.");
				});

		imapMailPollingAdapter.stop();
	}

}
