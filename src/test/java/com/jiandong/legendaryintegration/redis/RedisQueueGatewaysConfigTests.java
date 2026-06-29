package com.jiandong.legendaryintegration.redis;

import com.jiandong.legendaryintegration.config.RedisConfig;
import com.jiandong.legendaryintegration.testcontainer.RedisContainerTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@EnableIntegration
@SpringJUnitConfig({RedisQueueGatewaysConfig.class, RedisConfig.class})
@DirtiesContext
class RedisQueueGatewaysConfigTests implements RedisContainerTest {

	@Autowired
	@Qualifier("queueOutboundGatewayFlow.input")
	MessageChannel queueOutboundGatewayInput;

	@Test
	void happyFlow() {
		QueueChannel replyChannel = new QueueChannel();
		String gatewayMessagePayload = "queue-gateway-message";
		Message<String> gatewayMessage = MessageBuilder.withPayload(gatewayMessagePayload)
				.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel)
				.build();

		queueOutboundGatewayInput.send(gatewayMessage);
		Message<?> message = replyChannel.receive(10000);

		Assertions.assertThat(message)
				.extracting(Message::getPayload)
				.isEqualTo("Acked:" + gatewayMessagePayload);

	}

}
