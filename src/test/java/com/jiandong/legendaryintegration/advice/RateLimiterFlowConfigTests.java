package com.jiandong.legendaryintegration.advice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.handler.advice.RateLimiterRequestHandlerAdvice;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@EnableIntegration
@SpringJUnitConfig(RateLimiterFlowConfig.class)
@DirtiesContext
class RateLimiterFlowConfigTests {

	@Autowired
	@Qualifier("requestLimitedFlow.input")
	MessageChannel rateLimitInputChannel;

	@Test
	void rateLimitTest() throws InterruptedException {
		Message<String> message = MessageBuilder.withPayload("test").build();

		int messageCount = 3;
		AtomicBoolean rateLimited = new AtomicBoolean(false);
		CountDownLatch latch = new CountDownLatch(messageCount);
		for (int i = 0; i < messageCount; i++) {
			Thread.ofVirtual().start(() -> {
				try {
					rateLimitInputChannel.send(message);
				}
				catch (RateLimiterRequestHandlerAdvice.RateLimitExceededException ex) {
					System.out.println(ex.getMessage());
					rateLimited.set(true);
				}
				finally {
					latch.countDown();
				}
			});
		}
		latch.await();

		Assertions.assertThat(rateLimited).isTrue();
	}

}
