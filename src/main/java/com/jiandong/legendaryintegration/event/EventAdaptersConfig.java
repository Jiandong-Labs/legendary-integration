package com.jiandong.legendaryintegration.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.event.core.MessagingEvent;
import org.springframework.integration.event.inbound.ApplicationEventListeningMessageProducer;
import org.springframework.integration.event.outbound.ApplicationEventPublishingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

@Profile("event")
@Configuration
public class EventAdaptersConfig implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(EventAdaptersConfig.class);

	@Override
	public void run(ApplicationArguments args) throws Exception {
		publishEventChannel().send(MessageBuilder.withPayload("this is a event").build());
	}

	@Bean
	public MessageChannel publishEventChannel() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow outboundEventFlow() {
		var messageHandler = new ApplicationEventPublishingMessageHandler();
		messageHandler.setPublishPayload(false);
		return IntegrationFlow.from(publishEventChannel())
				.handle(messageHandler)
				.get();
	}

	@Bean
	public IntegrationFlow inboundEventFlow() {
		var messageProducer = new ApplicationEventListeningMessageProducer();
		messageProducer.setEventTypes(MessagingEvent.class);
		return IntegrationFlow.from(messageProducer)
				.handle(message -> log.info(message.toString()))
				.get();

	}

}
