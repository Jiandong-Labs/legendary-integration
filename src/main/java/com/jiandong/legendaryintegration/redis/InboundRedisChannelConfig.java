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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.redis.dsl.Redis;

@Profile("redis")
@Configuration
public class InboundRedisChannelConfig implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(InboundRedisChannelConfig.class);

	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Bean
	public IntegrationFlow inboundRedisChannel(RedisConnectionFactory redisConnectionFactory) {
		return IntegrationFlow.from(Redis
						.inboundChannelAdapter(redisConnectionFactory)
						.topics("inboundTopic"))
				.handle((GenericHandler<String>) (payload, headers) -> {
					log.info(headers.toString());
					log.info(payload);
					return null;
				})
				.get();
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		stringRedisTemplate.convertAndSend("inboundTopic", "hello");
	}

}
