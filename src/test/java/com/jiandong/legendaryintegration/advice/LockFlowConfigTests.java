package com.jiandong.legendaryintegration.advice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@EnableIntegration
@SpringJUnitConfig({LockFlowConfig.class})
@DirtiesContext
class LockFlowConfigTests {

	@Autowired
	@Qualifier("lockFlow.input")
	MessageChannel lockFlowInput;

	@Test
	void lockFlowTest() throws InterruptedException {
		int reqCnt = 2;
		AtomicReference<Message<String>> undeliveredMsg = new AtomicReference<>();
		CountDownLatch latch = new CountDownLatch(reqCnt);
		for (int i = 1; i <= reqCnt; i++) {
			Message<String> msg = MessageBuilder.withPayload("test" + i).setHeader("lockKey", "abc").build();
			Thread.ofVirtual().start(() -> {
				try {
					lockFlowInput.send(msg);
				}
				catch (MessageHandlingException ex) {
					undeliveredMsg.set(msg);
					Assertions.assertThat(ex).hasMessageContaining("Could not acquire the lock in time");
				}
				finally {
					latch.countDown();
				}
			});
		}
		latch.await();

		Assertions.assertThat(undeliveredMsg.get()).isNotNull();

	}

}
