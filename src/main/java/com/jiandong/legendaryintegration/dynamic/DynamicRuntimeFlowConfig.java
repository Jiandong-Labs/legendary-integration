package com.jiandong.legendaryintegration.dynamic;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.messaging.MessageHeaders;

@Configuration
public class DynamicRuntimeFlowConfig {

	private static final Logger log = LoggerFactory.getLogger(DynamicRuntimeFlowConfig.class);

	private final IntegrationFlowContext integrationFlowContext;

	public DynamicRuntimeFlowConfig(IntegrationFlowContext integrationFlowContext) {
		this.integrationFlowContext = integrationFlowContext;
	}

	@EventListener(ContextRefreshedEvent.class)
	public void createDynamicFlows() {
		integrationFlowContext.registration(IntegrationFlow
						.fromSupplier(new AtomicInteger(0)::incrementAndGet, e -> e
								.poller(Pollers.fixedDelay(10 * 1000)))
						.enrichHeaders(headers -> headers.header("source", "source1"))
						.handle(this::sharedHandler)
						.channel("sharedOutputChannel")
						.get()
				)
				.id("dynamicFlow1")
				.autoStartup(false)
				.register();

		integrationFlowContext.registration(IntegrationFlow
						.fromSupplier(new AtomicInteger(0)::incrementAndGet, e -> e
								.poller(Pollers.fixedDelay(10 * 1000)))
						.enrichHeaders(headers -> headers.header("source", "source2"))
						.handle(this::sharedHandler)
						.channel("sharedOutputChannel")
						.get()
				)
				.id("dynamicFlow2")
				.autoStartup(false)
				.register();
	}

	private String sharedHandler(Integer payload, MessageHeaders headers) {
		String source = (String) headers.get("source");
		log.info("handle message: {} from source {}", payload, source);
		return source + ":" + payload;
	}

	@Bean
	public QueueChannel sharedOutputChannel() {
		return new QueueChannel();
	}

}
