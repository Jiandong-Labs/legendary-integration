package com.jiandong.legendaryintegration.redis;

import com.jiandong.legendaryintegration.config.RedisConfig;
import com.jiandong.legendaryintegration.testcontainer.RedisContainerTest;
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
@SpringJUnitConfig({RedisOutboundCommandGatewayConfig.class, RedisConfig.class})
@DirtiesContext
class RedisOutboundCommandGatewayConfigTests implements RedisContainerTest {

	@Autowired
	@Qualifier("outboundCommandGatewayFlow.input")
	MessageChannel OutboundCommandGatewayInput;

	@Test
	void happyFlow() {
		QueueChannel replyChannel = new QueueChannel();
		OutboundCommandGatewayInput.send(MessageBuilder.withPayload("counter").setReplyChannel(replyChannel).build());

		Message<?> replyMsg = replyChannel.receive();

		Assertions.assertThat(replyMsg)
				.extracting(Message::getPayload)
				.isEqualTo(1L);
	}

}
