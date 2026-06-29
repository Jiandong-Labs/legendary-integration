package com.jiandong.legendaryintegration.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.redis.dsl.Redis;

@Configuration
public class RedisOutboundCommandGatewayConfig {

	@Bean
	public IntegrationFlow outboundCommandGatewayFlow(RedisConnectionFactory redisConnectionFactory) {
		return flow -> flow
				.handle(Redis.outboundGateway(redisConnectionFactory)
						.command("INCR"));
	}

}
