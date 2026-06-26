package com.jiandong.legendaryintegration.testcontainer;

import java.time.Duration;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Testcontainers(disabledWithoutDocker = true)
public interface RedisContainerTest {

	GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>("redis:8.4.0")
			.withExposedPorts(6379);

	@BeforeAll
	static void startContainer() {
		REDIS_CONTAINER.start();
	}

	static LettuceConnectionFactory connectionFactory() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setPort(REDIS_CONTAINER.getFirstMappedPort());

		LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
				.clientOptions(ClientOptions.builder()
						.socketOptions(
								SocketOptions.builder()
										.connectTimeout(Duration.ofMillis(10000))
										.keepAlive(true)
										.build())
						.build())
				.commandTimeout(Duration.ofSeconds(10000))
				.build();

		var connectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfiguration);
		connectionFactory.afterPropertiesSet();
		return connectionFactory;
	}

}
