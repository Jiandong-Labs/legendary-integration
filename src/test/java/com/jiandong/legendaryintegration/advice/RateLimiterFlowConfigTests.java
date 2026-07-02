package com.jiandong.legendaryintegration.advice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import com.jiandong.legendaryintegration.config.IntegrationErrorConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.handler.advice.RateLimiterRequestHandlerAdvice;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@EnableIntegration
@SpringJUnitConfig({RateLimiterFlowConfig.class, IntegrationErrorConfig.class})
@DirtiesContext
class RateLimiterFlowConfigTests {

	@Autowired
	@Qualifier("rateLimitedFlow.input")
	MessageChannel rateLimitedInputChannel;

	@Autowired
	@Qualifier("rateLimitedErrorOutputChannel")
	QueueChannel rateLimitedErrorOutputChannel;

	@Test
	void rateLimitTest() throws InterruptedException {
		int reqCnt = 3;
		AtomicReference<Message<String>> undeliveredMsg = new AtomicReference<>();
		CountDownLatch latch = new CountDownLatch(reqCnt);
		for (int i = 1; i <= reqCnt; i++) {
			Message<String> msg = MessageBuilder.withPayload("test" + i).build();
			Thread.ofVirtual().start(() -> {
				try {
					rateLimitedInputChannel.send(msg);
				}
				catch (RateLimiterRequestHandlerAdvice.RateLimitExceededException ex) {
					undeliveredMsg.set(msg);
				}
				finally {
					latch.countDown();
				}
			});
		}
		latch.await();

		Assertions.assertThat(undeliveredMsg.get()).isNotNull();

		Message<?> message = rateLimitedErrorOutputChannel.receive(10000);
		Assertions.assertThat(message).isInstanceOfSatisfying(ErrorMessage.class, errorMessage ->
				Assertions.assertThat((MessagingException) errorMessage.getPayload())
						.extracting(MessagingException::getFailedMessage)
						.extracting(Message::getPayload)
						.isEqualTo(undeliveredMsg.get().getPayload()));

	}

}
