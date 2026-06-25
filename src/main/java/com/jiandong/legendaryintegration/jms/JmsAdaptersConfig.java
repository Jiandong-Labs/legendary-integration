package com.jiandong.legendaryintegration.jms;

import jakarta.jms.ConnectionFactory;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.jms.dsl.Jms;

@Configuration
public class JmsAdaptersConfig {

	private static final JsonMapper JSON_MAPPER = new JsonMapper();

	@Bean
	public IntegrationFlow jmsOutboundMessageFlow(ConnectionFactory connectionFactory) {
		return flow -> flow
				.handle(Jms.outboundAdapter(connectionFactory)
						.extractPayload(true)
						.destination("test-queue"));
	}

	@Bean
	public IntegrationFlow jmsMessageDrivenFlow(ConnectionFactory connectionFactory) {
		return IntegrationFlow.from(Jms.messageDrivenChannelAdapter(connectionFactory)
						.id("jmsMessageProducer")
						.autoStartup(false)
						.destination("test-queue"))
				.transform(String.class, source -> JSON_MAPPER.readValue(source, Point.class))
				.<Point>handle((payload, headers) ->
						new Point(payload.x * 2, payload.y * 2))
				.channel("jmsOutputChannel")
				.get();
	}

	@Bean
	public QueueChannel jmsOutputChannel() {
		return new QueueChannel();
	}

	public record Point(int x, int y) {

	}

}
