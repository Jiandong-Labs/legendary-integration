package com.jiandong.legendaryintegration.controlbus;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.handler.annotation.Header;

@Configuration(proxyBeanMethods = false)
class ControlBusGatewayConfig {

	@Bean
	IntegrationFlow gatewayControlBusFlow() {
		return IntegrationFlow.from(ControlBusGateway.class, spec -> spec
						.beanName("controlBusGateway")
						.replyTimeout(500))
				.enrichHeaders(Map.of("source", "gateway"))
				.controlBus()
				.handle((p, h) -> h.get("source") + ":" + p)
				.get();
	}

	interface ControlBusGateway {

		@Nullable Object send(String command);

		@Nullable Object send(String command, @Header(IntegrationMessageHeaderAccessor.CONTROL_BUS_ARGUMENTS) List<?> args);

	}

}

