package com.jiandong.legendaryintegration.redis;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.redis.dsl.Redis;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

@Profile("redis")
@Configuration
public class OutboundRedisChannelConfig implements ApplicationRunner {

	@Bean
	public MessageChannel writeRedisChannel() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow outboundRedisFlow(RedisConnectionFactory redisConnectionFactory) {
		return IntegrationFlow.from(writeRedisChannel())
				.handle(Redis
						.outboundChannelAdapter(redisConnectionFactory)
						.topic("outboundTopic"))
				.get();
	}

	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new ChannelTopic("outboundTopic"));
		return container;
	}

	@Bean
	MessageListenerAdapter listenerAdapter() {
		return new MessageListenerAdapter() {

			@Override
			public void onMessage(Message message, byte @Nullable [] pattern) {
				logger.info("Received message from channel: " + new String(message.getChannel()));
				logger.info("Received message body: " + new String(message.getBody()));
			}
		};
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		writeRedisChannel().send(MessageBuilder.withPayload("Hello World!").build());
	}

}
