package com.jiandong.legendaryintegration.redis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.redis.dsl.Redis;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

@Profile("redis")
@Configuration
public class QueueOutboundChannelConfig implements ApplicationRunner {

	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Bean
	public MessageChannel writeRedisQueueChannel() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow queueOutboundChannel(RedisConnectionFactory connectionFactory) {
		return IntegrationFlow.from(writeRedisQueueChannel())
				.handle(Redis
						.queueOutboundChannelAdapter("redis-queue", connectionFactory)
						.leftPush(true)
						.extractPayload(true))
				.get();
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		writeRedisQueueChannel().send(MessageBuilder.withPayload("item1").build());
		writeRedisQueueChannel().send(MessageBuilder.withPayload("item2").build());

		var listOps = stringRedisTemplate.boundListOps("redis-queue");
		List<String> items = listOps.rightPop(2);
		System.out.println(items);
	}

}
