package com.jiandong.legendaryintegration.advice;

import java.util.ArrayList;
import java.util.List;

import com.jiandong.legendaryintegration.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@IntegrationTest({CircuitBreakerFlowConfig.class})
class CircuitBreakerFlowConfigTests {

	@Autowired
	@Qualifier("circuitBreakerFlow.input")
	MessageChannel circuitBreakerInputChannel;

	@Test
	void circuitBreakerFlowTest() throws InterruptedException {
		List<Message<?>> circuitBreakerMessages = new ArrayList<>();
		for (int i = 1; i <= 5; i++) {
			Message<String> msg = MessageBuilder.withPayload("test" + i).build();
			try {
				circuitBreakerInputChannel.send(msg);
			}
			catch (RequestHandlerCircuitBreakerAdvice.CircuitBreakerOpenException circuitBreakerOpenException) {
				circuitBreakerMessages.add(msg);
				Thread.sleep(1000); // make circuit breaker half open
			}
			catch (Exception ignore) {

			}

		}

		Assertions.assertThat(circuitBreakerMessages).hasSize(2);

		Assertions.assertThat(circuitBreakerMessages).first()
				.extracting(Message::getPayload)
				.isEqualTo("test3");

		Assertions.assertThat(circuitBreakerMessages).last()
				.extracting(Message::getPayload)
				.isEqualTo("test5");
	}

}
