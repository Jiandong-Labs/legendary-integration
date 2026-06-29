package com.jiandong.legendaryintegration.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.collections.RedisCollectionFactoryBean;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.redis.dsl.Redis;

@Configuration
public class RedisStoreAdaptersConfig {

	@Bean
	public IntegrationFlow redisStoreOutboundFlow(RedisConnectionFactory redisConnectionFactory) {
		return flow -> flow
				.handle(Redis.storeOutboundChannelAdapter(redisConnectionFactory)
						.collectionType(RedisCollectionFactoryBean.CollectionType.ZSET)
						.key("tasks-zset-store")
						.extractPayloadElements(true));
	}

	@Bean
	public IntegrationFlow redisStoreInboundFlow(RedisConnectionFactory redisConnectionFactory) {
		return IntegrationFlow.from(Redis
						.storeInboundChannelAdapter(redisConnectionFactory, "tasks-zset-store")
						.collectionType(RedisCollectionFactoryBean.CollectionType.ZSET), e -> e
						.id("redisStoreSourcePollingAdapter")
						.autoStartup(false)
						.poller(Pollers.fixedDelay(1000)))
				.channel("redisZestOutputChannel")
				.get();
	}

	@Bean
	QueueChannel redisZestOutputChannel() {
		return new QueueChannel();
	}

}
