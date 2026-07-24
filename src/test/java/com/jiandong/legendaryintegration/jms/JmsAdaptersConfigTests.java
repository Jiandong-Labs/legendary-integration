package com.jiandong.legendaryintegration.jms;

import com.jiandong.legendaryintegration.config.ActivemqConfig;
import com.jiandong.legendaryintegration.testcontainer.ActivemqContainerTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@EnableIntegration
@SpringJUnitConfig({JmsAdaptersConfig.class, ActivemqConfig.class})
@DirtiesContext
class JmsAdaptersConfigTests implements ActivemqContainerTest {

	static final JsonMapper JSON_MAPPER = new JsonMapper();

	@Autowired
	@Qualifier("jmsOutboundMessageFlow.input")
	MessageChannel jmsInputChannel;

	@Autowired
	@Qualifier("jmsMessageProducer")
	AbstractEndpoint jmsMessageProducer;

	@Autowired
	QueueChannel jmsOutputChannel;

	@Test
	void happyFlow() {
		jmsMessageProducer.start();

		String payload = JSON_MAPPER.writeValueAsString(new JmsAdaptersConfig.Point(1, 2));
		jmsInputChannel.send(MessageBuilder.withPayload(payload).build());

		Message<?> message = jmsOutputChannel.receive(10000);
		Assertions.assertThat(message)
				.extracting(Message::getPayload)
				.isEqualTo(new JmsAdaptersConfig.Point(2, 4));

		jmsMessageProducer.stop();
	}

}
