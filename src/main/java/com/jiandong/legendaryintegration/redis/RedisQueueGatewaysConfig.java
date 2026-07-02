package com.jiandong.legendaryintegration.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.redis.dsl.Redis;

@Configuration
public class RedisQueueGatewaysConfig {

	@Bean
	IntegrationFlow queueOutboundGatewayFlow(RedisConnectionFactory redisConnectionFactory) {
		return flow -> flow
				.handle(Redis.queueOutboundGateway("queueGateways", redisConnectionFactory)
						.serializer(RedisSerializer.string())
						.extractPayload(true)
						.receiveTimeout(20000), e -> e
						.sendTimeout(10000));
	}

	@Bean
	IntegrationFlow queueInboundGatewayFlow(RedisConnectionFactory redisConnectionFactory) {
		return IntegrationFlow.from(Redis
						.queueInboundGateway("queueGateways", redisConnectionFactory)
						.id("redisInboundGatewayEndpoint")
						.autoStartup(false)
						.serializer(RedisSerializer.string())
						.receiveTimeout(10000))
				.handle((uuidValue, headers) -> "Acked:" + uuidValue)
				.get();
	}

}
