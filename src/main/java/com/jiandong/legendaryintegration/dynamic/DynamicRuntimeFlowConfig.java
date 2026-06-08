package com.jiandong.legendaryintegration.dynamic;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.messaging.Message;

@Profile("dynamic-runtime-flow")
@Configuration
public class DynamicRuntimeFlowConfig implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger log = LoggerFactory.getLogger(DynamicRuntimeFlowConfig.class);

	private final IntegrationFlowContext integrationFlowContext;

	public DynamicRuntimeFlowConfig(IntegrationFlowContext integrationFlowContext) {
		this.integrationFlowContext = integrationFlowContext;
	}

	private final AtomicInteger counter1 = new AtomicInteger(0);

	private final AtomicInteger counter2 = new AtomicInteger(0);

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		integrationFlowContext.registration(IntegrationFlow
						.fromSupplier(counter1::getAndIncrement, e -> e
								.poller(Pollers.fixedDelay(10 * 1000)))
						.enrichHeaders(headers -> headers.header("source", "source1"))
						.handle(this::sharedHandler)
						.get()
				)
				.id("dynamicFlow1")
				.register();

		integrationFlowContext.registration(IntegrationFlow
						.fromSupplier(counter2::getAndIncrement, e -> e
								.poller(Pollers.fixedDelay(10 * 1000)))
						.enrichHeaders(headers -> headers.header("source", "source2"))
						.handle(this::sharedHandler)
						.get()
				)
				.id("dynamicFlow2")
				.register();
	}

	private void sharedHandler(Message<?> message) {
		log.info("handle message: {} from source {}", message.getPayload(), message.getHeaders().get("source"));
	}

}
