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
@SpringJUnitConfig({RedisTopicAdaptersConfig.class, RedisConfig.class})
@DirtiesContext
class RedisTopicAdaptersConfigTests implements RedisContainerTest {

	@Autowired
	@Qualifier("publishingTopicFlow.input")
	MessageChannel publishingTopicInput;

	@Autowired
	QueueChannel topicOutputChannel;

	@Test
	void happyFlow() {
		publishingTopicInput.send(MessageBuilder.withPayload("hello").build());

		Message<?> message = topicOutputChannel.receive(10000);

		Assertions.assertThat(message).isNotNull();
		Assertions.assertThat(message.getPayload()).isEqualTo("hello");
		Assertions.assertThat(message.getHeaders()).containsEntry("redis_messageSource", "newsTopic");
	}

}
