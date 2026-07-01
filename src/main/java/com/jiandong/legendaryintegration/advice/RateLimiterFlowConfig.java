package com.jiandong.legendaryintegration.advice;

import java.time.Duration;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.advice.RateLimiterRequestHandlerAdvice;

@Configuration(proxyBeanMethods = false)
public class RateLimiterFlowConfig {

	private static final Logger log = LoggerFactory.getLogger(RateLimiterFlowConfig.class);

	@Bean
	public RateLimiterRequestHandlerAdvice rateLimiterRequestHandlerAdvice() {
		return new RateLimiterRequestHandlerAdvice(RateLimiterConfig.custom()
				.limitRefreshPeriod(Duration.ofSeconds(1))
				.limitForPeriod(1)
				.timeoutDuration(Duration.ofSeconds(1))
				.build(), "downstream limiter");
	}

	@Bean
	public IntegrationFlow requestLimitedFlow(RateLimiterRequestHandlerAdvice rateLimiterRequestHandlerAdvice) {
		return flow -> flow
				.<String>handle((payload, headers) -> {
					log.info(payload);
					return null;
				}, e -> e
						.id("limitedHandler")
						.advice(rateLimiterRequestHandlerAdvice));
	}

}
