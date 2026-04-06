package com.jiandong.legendaryintegration.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.redis.dsl.Redis;

@Profile("redis")
@Configuration
public class QueueInboundChannelConfig implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(QueueInboundChannelConfig.class);

	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Bean
	public IntegrationFlow queueInboundConfig(RedisConnectionFactory redisConnectionFactory) {
		return IntegrationFlow.from(Redis
						.queueInboundChannelAdapter("redis-queue", redisConnectionFactory)
						.serializer(RedisSerializer.string())
						.rightPop(true))
				.handle(p -> log.info("Inbound channel handler received : {}", p))
				.get();
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		BoundListOperations<String, String> ops = stringRedisTemplate.boundListOps("redis-queue");
		ops.leftPush("hello");
		ops.leftPush("world");
	}

}
