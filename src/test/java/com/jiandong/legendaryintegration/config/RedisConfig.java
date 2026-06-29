package com.jiandong.legendaryintegration.config;

import com.jiandong.legendaryintegration.testcontainer.RedisContainerTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@TestConfiguration(proxyBeanMethods = false)
public class RedisConfig {

	@Bean
	LettuceConnectionFactory connectionFactory() {
		return RedisContainerTest.connectionFactory();
	}

}
