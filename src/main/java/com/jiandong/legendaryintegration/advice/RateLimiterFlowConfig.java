package com.jiandong.legendaryintegration.advice;

import java.time.Duration;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.handler.advice.RateLimiterRequestHandlerAdvice;
import org.springframework.messaging.MessagingException;

import static com.jiandong.legendaryintegration.util.Constants.ROUTER_KEY;
import static com.jiandong.legendaryintegration.util.Constants.ROUTER_VAL_RATE_LIMIT;
import static com.jiandong.legendaryintegration.util.Constants.ROUTER_VAL_RATE_LIMIT_CHANNEL;

@Configuration(proxyBeanMethods = false)
class RateLimiterFlowConfig {

	private static final Logger log = LoggerFactory.getLogger(RateLimiterFlowConfig.class);

	@Bean
	RateLimiterRequestHandlerAdvice rateLimiterRequestHandlerAdvice() {
		return new RateLimiterRequestHandlerAdvice(RateLimiterConfig.custom()
				.limitRefreshPeriod(Duration.ofSeconds(1))
				.limitForPeriod(1)
				.timeoutDuration(Duration.ofSeconds(1))
				.build(), "downstream limiter");
	}

	@Bean
	ExpressionEvaluatingRequestHandlerAdvice errorHandlerAdvice() {
		var advice = new ExpressionEvaluatingRequestHandlerAdvice();
		advice.setFailureChannelName(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME);
		return advice;
	}

	@Bean
	IntegrationFlow rateLimitedFlow(RateLimiterRequestHandlerAdvice rateLimiterRequestHandlerAdvice,
			ExpressionEvaluatingRequestHandlerAdvice errorHandlerAdvice) {
		return flow -> flow
				.enrichHeaders(h -> h.header(ROUTER_KEY, ROUTER_VAL_RATE_LIMIT))
				.<String>handle((payload, headers) -> {
					log.info("payload: {}", payload);
					return null;
				}, e -> e
						.id("limitedHandler")
						.advice(errorHandlerAdvice)
						.advice(rateLimiterRequestHandlerAdvice));
	}

	@Bean
	IntegrationFlow rateLimitedErrorFlow() {
		return IntegrationFlow.from(ROUTER_VAL_RATE_LIMIT_CHANNEL)
				.<MessagingException>handle((p, h) -> {
					log.error("due to rate limitation, original message is not delivered : {}", p.getFailedMessage(), p.getMostSpecificCause());
					return p;
				})
				.channel("rateLimitedErrorOutputChannel")
				.get();
	}

	@Bean
	QueueChannel rateLimitedErrorOutputChannel() {
		return new QueueChannel();
	}

}
