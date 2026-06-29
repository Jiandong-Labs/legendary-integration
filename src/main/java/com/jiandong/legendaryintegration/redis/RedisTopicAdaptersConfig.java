package com.jiandong.legendaryintegration.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.redis.dsl.Redis;

@Configuration
public class RedisTopicAdaptersConfig {

	private static final String TOPIC = "newsTopic";

	@Bean
	public IntegrationFlow publishingTopicFlow(RedisConnectionFactory redisConnectionFactory) {
		return flow -> flow
				.handle(Redis
						.outboundChannelAdapter(redisConnectionFactory)
						.topic(TOPIC));
	}

	@Bean
	public IntegrationFlow subscribeTopicFlow(RedisConnectionFactory redisConnectionFactory) {
		return IntegrationFlow.from(Redis
						.inboundChannelAdapter(redisConnectionFactory)
						.topics(TOPIC))
				.log()
				.channel("topicOutputChannel")
				.get();
	}

	@Bean
	public QueueChannel topicOutputChannel() {
		return new QueueChannel();
	}

}
