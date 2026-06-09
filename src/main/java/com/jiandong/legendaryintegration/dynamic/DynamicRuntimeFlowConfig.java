package com.jiandong.legendaryintegration.dynamic;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.messaging.Message;

@Profile("dynamic-runtime-flow")
@Configuration
public class DynamicRuntimeFlowConfig implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger log = LoggerFactory.getLogger(DynamicRuntimeFlowConfig.class);

	private final IntegrationFlowContext integrationFlowContext;

	private final AtomicInteger flow1Source = new AtomicInteger(0);

	private final AtomicInteger flow2Source = new AtomicInteger(0);

	private IntegrationFlowContext.IntegrationFlowRegistration flow1Registration;

	private IntegrationFlowContext.IntegrationFlowRegistration flow2Registration;

	public DynamicRuntimeFlowConfig(IntegrationFlowContext integrationFlowContext) {
		this.integrationFlowContext = integrationFlowContext;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		flow1Registration = integrationFlowContext.registration(IntegrationFlow
						.fromSupplier(flow1Source::getAndIncrement, e -> e
								.poller(Pollers.fixedDelay(10 * 1000)))
						.enrichHeaders(headers -> headers.header("source", "source1"))
						.handle(this::sharedHandler)
						.get()
				)
				.id("dynamicFlow1")
				.register();

		flow2Registration = integrationFlowContext.registration(IntegrationFlow
						.fromSupplier(flow2Source::getAndIncrement, e -> e
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

	@PreDestroy
	public void destroy() {
		flow1Registration.destroy();
		flow2Registration.destroy();
	}

}
