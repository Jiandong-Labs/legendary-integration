package com.jiandong.legendaryintegration.redis;

import com.jiandong.legendaryintegration.testcontainer.RedisContainerTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@EnableIntegration
@SpringJUnitConfig({RedisQueueAdaptersConfig.class, RedisQueueAdaptersConfigTests.Config.class})
@DirtiesContext
class RedisQueueAdaptersConfigTests implements RedisContainerTest {

	@Autowired
	@Qualifier("pushQueueFlow.input")
	MessageChannel pushQueueFlowInput;

	@Autowired
	QueueChannel queueOutputChannel;

	@Test
	void happyFlow() {
		pushQueueFlowInput.send(MessageBuilder.withPayload("1").build());
		pushQueueFlowInput.send(MessageBuilder.withPayload("2").build());
		pushQueueFlowInput.send(MessageBuilder.withPayload("3").build());

		Message<?> message1 = queueOutputChannel.receive(10000);
		Assertions.assertThat(message1).extracting(Message::getPayload).isEqualTo("1");

		Message<?> message2 = queueOutputChannel.receive(10000);
		Assertions.assertThat(message2).extracting(Message::getPayload).isEqualTo("2");

		Message<?> message3 = queueOutputChannel.receive(10000);
		Assertions.assertThat(message3).extracting(Message::getPayload).isEqualTo("3");
	}

	@Configuration(proxyBeanMethods = false)
	static class Config {

		@Bean
		LettuceConnectionFactory connectionFactory() {
			return RedisContainerTest.connectionFactory();
		}

	}

}
