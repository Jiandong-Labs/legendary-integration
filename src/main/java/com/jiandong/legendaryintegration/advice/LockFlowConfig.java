package com.jiandong.legendaryintegration.advice;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.advice.LockRequestHandlerAdvice;
import org.springframework.integration.support.locks.DefaultLockRegistry;

@Configuration(proxyBeanMethods = false)
class LockFlowConfig {

	private static final Logger log = LoggerFactory.getLogger(LockFlowConfig.class);

	@Bean
	LockRequestHandlerAdvice lockAdvice() {
		var lockRegistry = new DefaultLockRegistry();
		var advice = new LockRequestHandlerAdvice(lockRegistry, msg -> msg.getHeaders().get("lockKey"));
		advice.setWaitLockDuration(Duration.ZERO);
		return advice;
	}

	@Bean
	IntegrationFlow lockFlow(LockRequestHandlerAdvice lockAdvice) {
		return flow -> flow
				.<String>handle((p, h) -> {
					try {
						Thread.sleep(200);
					}
					catch (InterruptedException ignore) {
					}
					log.info(p);
					return null;
				}, e -> e
						.id("lockAwareHandler")
						.advice(lockAdvice));
	}

}
