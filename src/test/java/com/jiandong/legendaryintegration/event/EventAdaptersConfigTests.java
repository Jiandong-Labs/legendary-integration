package com.jiandong.legendaryintegration.event;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@EnableIntegration
@SpringJUnitConfig(EventAdaptersConfig.class)
@DirtiesContext
class EventAdaptersConfigTests {

	@Autowired
	@Qualifier("publishEventFlow.input")
	MessageChannel inputChannel;

	@Autowired
	QueueChannel eventOutputChannel;

	@Test
	void happyFlow() {
		inputChannel.send(MessageBuilder.withPayload("this is a event").build());
		Message<?> message = eventOutputChannel.receive(10000);

		Assertions.assertThat(message)
				.isNotNull()
				.extracting(Message::getPayload)
				.isInstanceOf(String.class)
				.isEqualTo("THIS IS A EVENT");
	}

}
