package com.jiandong.legendaryintegration.jms;

import jakarta.jms.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

@Profile("jms")
@Configuration
public class JmsAdaptersConfig implements ApplicationRunner {

	private static final JsonMapper JSON_MAPPER = new JsonMapper();

	private static final Logger log = LoggerFactory.getLogger(JmsAdaptersConfig.class);

	@Override
	public void run(ApplicationArguments args) throws Exception {
		inputChannel().send(MessageBuilder.withPayload(JSON_MAPPER.writeValueAsString(new Point(1, 2))).build());
	}

	@Bean
	public MessageChannel inputChannel() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow outboundEventFlow(ConnectionFactory connectionFactory) {
		return IntegrationFlow.from(inputChannel())
				.handle(Jms.outboundAdapter(connectionFactory)
						.extractPayload(true)
						.destination("test-queue"))
				.get();
	}

	@Bean
	public IntegrationFlow messageDrivenFlow(ConnectionFactory connectionFactory) {
		return IntegrationFlow.from(Jms.messageDrivenChannelAdapter(connectionFactory)
						.destination("test-queue"))
				.transform(String.class, source -> JSON_MAPPER.readValue(source, Point.class))
				.handle(Point.class, (payload, headers) ->
						new Point(payload.x * 2, payload.y * 2))
				.handle((GenericHandler<Point>) (payload, headers) -> {
					log.info(payload.toString());
					log.info(headers.toString());
					return null;
				})
				.get();
	}

	public record Point(int x, int y) {

	}

}
