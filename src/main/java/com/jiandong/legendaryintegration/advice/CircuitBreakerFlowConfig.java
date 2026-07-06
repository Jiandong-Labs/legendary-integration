package com.jiandong.legendaryintegration.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice;

@Configuration(proxyBeanMethods = false)
public class CircuitBreakerFlowConfig {

	private static final Logger log = LoggerFactory.getLogger(CircuitBreakerFlowConfig.class);

	@Bean
	public RequestHandlerCircuitBreakerAdvice circuitBreakerAdvice() {
		var circuitBreakerAdvice = new RequestHandlerCircuitBreakerAdvice();

		circuitBreakerAdvice.setThreshold(2);
		circuitBreakerAdvice.setHalfOpenAfter(1000);

		return circuitBreakerAdvice;
	}

	@Bean
	public IntegrationFlow circuitBreakerFlow(RequestHandlerCircuitBreakerAdvice circuitBreakerAdvice) {
		return flow -> flow
				.<String>handle((p, h) -> {
					log.info("payload: {}", p);
					throw new RuntimeException("simulate downstream error.");
				}, e -> e
						.id("circuitBreakerHandler")
						.advice(circuitBreakerAdvice));
	}

}
