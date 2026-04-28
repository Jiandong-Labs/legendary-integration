package com.jiandong.legendaryintegration.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.redis.dsl.Redis;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

@Profile("redis")
@Configuration
public class QueueGatewaysConfig implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(QueueGatewaysConfig.class);

	@Bean
	public MessageChannel writeRedisGatewayQueueChannel() {
		return new DirectChannel();
	}

	@Bean
	IntegrationFlow queueOutboundGatewayFlow(RedisConnectionFactory redisConnectionFactory) {
		return IntegrationFlow.from(writeRedisGatewayQueueChannel())
				.handle(Redis.queueOutboundGateway("queueGateways", redisConnectionFactory)
						.serializer(RedisSerializer.string())
						.extractPayload(true)
						.receiveTimeout(20000), e -> e
						.sendTimeout(10000))
				.get();
	}

	@Bean
	IntegrationFlow queueInboundGatewayFlow(RedisConnectionFactory redisConnectionFactory) {
		return IntegrationFlow.from(Redis
						.queueInboundGateway("queueGateways", redisConnectionFactory)
						.serializer(RedisSerializer.string())
						.receiveTimeout(10000))
				.handle((uuidValue, headers) -> "Acked:" + uuidValue)
				.get();
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		QueueChannel replyChannel = new QueueChannel();
		String gatewayMessagePayload = "queue-gateway-message";
		Message<String> gatewayMessage = MessageBuilder.withPayload(gatewayMessagePayload)
				.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel)
				.build();
		writeRedisGatewayQueueChannel().send(gatewayMessage);
		Message<?> msg = replyChannel.receive(10000);
		log.info("Received Message: " + msg.getPayload());
	}

}
