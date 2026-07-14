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
						.replyTimeout(30_000))
				.enrichHeaders(Map.of("source", "gateway"))
				.controlBus()
				.handle((p, h) -> h.get("source") + ":" + p)
				.get();
	}

	interface ControlBusGateway {

		default @Nullable Object send(String command) {
			return send(command, null);
		}

		@Nullable Object send(String command, @Nullable @Header(IntegrationMessageHeaderAccessor.CONTROL_BUS_ARGUMENTS) List<?> args);

	}

}

