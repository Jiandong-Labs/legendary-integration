package com.jiandong.legendaryintegration.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.redis.dsl.Redis;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@Profile("redis")
@Configuration
public class OutboundCommandGatewayConfig implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(OutboundCommandGatewayConfig.class);

	@Bean
	public MessageChannel writeRedisCommandChannel() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow outboundCommandGatewayFlow(RedisConnectionFactory redisConnectionFactory) {
		return IntegrationFlow.from(writeRedisCommandChannel())
				.handle(Redis.outboundGateway(redisConnectionFactory)
						.command("INCR"))
				.get();
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		QueueChannel replyChannel = new QueueChannel();
		writeRedisCommandChannel().send(MessageBuilder
				.withPayload("counter")
				.setReplyChannel(replyChannel)
				.build());
		Message<?> replyMsg = replyChannel.receive();
		long counter = (long) replyMsg.getPayload();
		log.info("counter: " + counter);
	}

}
