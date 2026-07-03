package com.jiandong.legendaryintegration.advice;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.Retryable;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.advice.ErrorMessageSendingRecoverer;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.messaging.MessagingException;

import static com.jiandong.legendaryintegration.util.Constants.ROUTER_KEY;
import static com.jiandong.legendaryintegration.util.Constants.ROUTER_VAL_RETRY;
import static com.jiandong.legendaryintegration.util.Constants.ROUTER_VAL_RETRY_CHANNEL;

@Configuration(proxyBeanMethods = false)
public class RetryableFlowConfig {

	private static final Logger log = LoggerFactory.getLogger(RetryableFlowConfig.class);

	@Bean
	public RequestHandlerRetryAdvice retryAdvice(ErrorMessageSendingRecoverer retryRecoveryCallback) {
		var retryAdvice = new RequestHandlerRetryAdvice();

		retryAdvice.setRetryPolicy(RetryPolicy.builder().maxRetries(1).delay(Duration.ZERO).build());

		retryAdvice.setRetryListener(new RetryListener() {

			@Override
			public void beforeRetry(RetryPolicy retryPolicy, Retryable<?> retryable) {
				log.info("retry started for: {}", retryable.getName());
			}
		});

		retryAdvice.setRecoveryCallback(retryRecoveryCallback);

		return retryAdvice;
	}

	@Bean
	public ErrorMessageSendingRecoverer retryRecoveryCallback() {
		return new ErrorMessageSendingRecoverer(); // by default, sending to error channel
	}

	@Bean
	public IntegrationFlow retryableFlow(RequestHandlerRetryAdvice retryAdvice) {
		return flow -> flow
				.enrichHeaders(Map.of(ROUTER_KEY, ROUTER_VAL_RETRY))
				.<String>handle((payload, headers) -> {
					log.info("payload: {}", payload);
					throw new RuntimeException("an error happened with payload: " + payload);
				}, e -> e
						.id("retryableHandler")
						.advice(retryAdvice));
	}

	@Bean
	public IntegrationFlow retryableErrorFlow() {
		return IntegrationFlow.from(ROUTER_VAL_RETRY_CHANNEL)
				.<MessagingException>handle((p, h) -> {
					log.error("retry error: {}", p.getFailedMessage(), p.getMostSpecificCause());
					return p;
				})
				.channel("retryErrorOutputChannel")
				.get();
	}

	@Bean
	public QueueChannel retryErrorOutputChannel() {
		return new QueueChannel();
	}

}
