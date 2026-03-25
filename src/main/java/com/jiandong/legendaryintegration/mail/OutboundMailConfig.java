package com.jiandong.legendaryintegration.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.MessageChannel;

@Profile("mail")
@Configuration
public class OutboundMailConfig implements ApplicationRunner {

	@Value("${spring.mail.username}")
	private String from;

	@Override
	public void run(ApplicationArguments args) {
		mailSendChannel().send(MessageBuilder
				.withPayload("this is a test message using spring integration.")
				.build()
		);
	}

	@Bean
	public MessageChannel mailSendChannel() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow mailSendFlow(JavaMailSender javaMailSender) {
		return IntegrationFlow.from(mailSendChannel())
				.enrichHeaders(Mail.headers()
						.from(from)
						.to("mjd507@qq.com")
						.subject("Hello Jiandong"))
				.handle(Mail.outboundAdapter(javaMailSender))
				.get();
	}

}
