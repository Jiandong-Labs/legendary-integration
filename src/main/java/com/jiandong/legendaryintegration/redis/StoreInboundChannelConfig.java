package com.jiandong.legendaryintegration.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.collections.RedisCollectionFactoryBean.CollectionType;
import org.springframework.data.redis.support.collections.RedisZSet;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.redis.dsl.Redis;

@Profile("redis")
@Configuration
public class StoreInboundChannelConfig implements ApplicationRunner, ApplicationContextAware {

	private static final Logger log = LoggerFactory.getLogger(StoreInboundChannelConfig.class);

	@Autowired
	StringRedisTemplate stringRedisTemplate;

	private ApplicationContext applicationContext;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		var zSetOps = stringRedisTemplate.boundZSetOps("redis-store");
		zSetOps.add("task:1", 1);
		zSetOps.add("task:2", 2);
		zSetOps.add("task:3", 3);
		var storeSourcePollingChannelAdapter = applicationContext.getBean("storeSourcePollingChannelAdapter", SourcePollingChannelAdapter.class);
		storeSourcePollingChannelAdapter.start();
	}

	@Bean
	public IntegrationFlow storeInboundFlow(RedisConnectionFactory redisConnectionFactory) {
		return IntegrationFlow.from(Redis
						.storeInboundChannelAdapter(redisConnectionFactory, "redis-store")
						.collectionType(CollectionType.ZSET), e -> e
						.id("storeSourcePollingChannelAdapter")
						.autoStartup(false)
						.poller(Pollers.fixedDelay(1000)))
				.handle((GenericHandler<RedisZSet<String>>) (payload, headers) -> {
					log.info("Received message: " + payload.rangeByScore(1, 3));
					stringRedisTemplate.boundZSetOps("redis-store").removeRangeByScore(1, 3);
					return null;
				})
				.get();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
