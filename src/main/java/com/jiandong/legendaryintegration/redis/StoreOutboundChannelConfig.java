package com.jiandong.legendaryintegration.redis;

import java.util.Objects;

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
import org.springframework.data.redis.support.collections.RedisCollectionFactoryBean.CollectionType;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.redis.dsl.Redis;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

@Profile("redis")
@Configuration
public class StoreOutboundChannelConfig implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(StoreOutboundChannelConfig.class);

	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		MessageChannel messageChannel = writeRedisStoreChannel();
		messageChannel.send(MessageBuilder.withPayload("200").setHeader("productName", "mac").build());
		messageChannel.send(MessageBuilder.withPayload("300").setHeader("productName", "win").build());

		var entries = stringRedisTemplate.boundHashOps("products").entries();
		entries.forEach((key, value) -> log.info("key: {}, value: {}", key, value));
	}

	@Bean
	public MessageChannel writeRedisStoreChannel() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow storeOutboundFlow(RedisConnectionFactory redisConnectionFactory) {
		return IntegrationFlow.from(writeRedisStoreChannel())
				.handle(Redis.storeOutboundChannelAdapter(redisConnectionFactory)
						.collectionType(CollectionType.MAP)
						.key("products")
						.mapKeyFunction(msg -> Objects.requireNonNull(msg.getHeaders().get("productName")).toString())
						.extractPayloadElements(true))
				.get();
	}

}
