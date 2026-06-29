package com.jiandong.legendaryintegration.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.redis.dsl.Redis;

@Configuration
public class RedisQueueAdaptersConfig {

	@Bean
	public IntegrationFlow pushQueueFlow(RedisConnectionFactory connectionFactory) {
		return flow -> flow
				.handle(Redis
						.queueOutboundChannelAdapter("redis-queue", connectionFactory)
						.leftPush(true)
						.extractPayload(true));
	}

	@Bean
	public IntegrationFlow popQueueFlow(RedisConnectionFactory connectionFactory) {
		return IntegrationFlow.from(Redis
						.queueInboundChannelAdapter("redis-queue", connectionFactory)
						.serializer(RedisSerializer.string())
						.expectMessage(false)
						.rightPop(true))
				.log()
				.channel("queueOutputChannel")
				.get();
	}

	@Bean
	public QueueChannel queueOutputChannel() {
		return new QueueChannel();
	}

}
