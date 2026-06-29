package com.jiandong.legendaryintegration.redis;

import java.util.Map;
import java.util.Set;

import com.jiandong.legendaryintegration.config.RedisConfig;
import com.jiandong.legendaryintegration.testcontainer.RedisContainerTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.support.collections.RedisZSet;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@EnableIntegration
@SpringJUnitConfig({RedisStoreAdaptersConfig.class, RedisConfig.class})
@DirtiesContext
class RedisStoreAdaptersConfigTests implements RedisContainerTest {

	@Autowired
	@Qualifier("redisStoreOutboundFlow.input")
	MessageChannel redisStoreOutboundFlowInput;

	@Qualifier("redisStoreSourcePollingAdapter")
	@Autowired SourcePollingChannelAdapter redisStoreSourcePollingAdapter;

	@Autowired
	QueueChannel redisZestOutputChannel;

	@Test
	void happyFlow() {
		Message<?> inputMessage = MessageBuilder.withPayload(Map.of(
						"task1", 1,
						"task2", 2,
						"task3", 3))
				.build();

		redisStoreSourcePollingAdapter.start();

		redisStoreOutboundFlowInput.send(inputMessage);

		Message<?> message = redisZestOutputChannel.receive(10000);
		Assertions.assertThat(message)
				.extracting(Message::getPayload)
				.isInstanceOfSatisfying(RedisZSet.class, payload -> {
					Set<String> range = payload.rangeByScore(1, 3);
					Assertions.assertThat(range).containsExactly("task1", "task2", "task3");
				});
		redisStoreSourcePollingAdapter.stop();
	}

}
