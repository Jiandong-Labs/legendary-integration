package com.jiandong.legendaryintegration.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.event.core.MessagingEvent;
import org.springframework.integration.event.inbound.ApplicationEventListeningMessageProducer;
import org.springframework.integration.event.outbound.ApplicationEventPublishingMessageHandler;

@Configuration
public class EventAdaptersConfig {

	@Bean
	public IntegrationFlow publishEventFlow() {
		var messageHandler = new ApplicationEventPublishingMessageHandler();
		messageHandler.setPublishPayload(false);
		return flow -> flow.handle(messageHandler);
	}

	@Bean
	public IntegrationFlow subscribeEventFlow() {
		var messageProducer = new ApplicationEventListeningMessageProducer();
		messageProducer.setEventTypes(MessagingEvent.class);
		return IntegrationFlow.from(messageProducer)
				.<String>handle((p, h) -> p.toUpperCase())
				.channel("eventOutputChannel")
				.get();
	}

	@Bean
	public QueueChannel eventOutputChannel() {
		return new QueueChannel();
	}

}
