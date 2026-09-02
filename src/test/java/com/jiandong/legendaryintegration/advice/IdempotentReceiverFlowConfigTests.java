package com.jiandong.legendaryintegration.advice;

import com.jiandong.legendaryintegration.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.MessageRejectedException;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@IntegrationTest({IdempotentReceiverFlowConfig.class})
class IdempotentReceiverFlowConfigTests {

	@Autowired
	@Qualifier("idempotentFlow.input")
	MessageChannel idempotentFlowInput;

	@Test
	void idempotentFlowTest() {
		Message<String> msg1 = MessageBuilder.withPayload("test").setHeader("unique_key", "123").build();
		idempotentFlowInput.send(msg1);

		Message<String> msg2 = MessageBuilder.withPayload("test").setHeader("unique_key", "123").build();

		Assertions.assertThatThrownBy(() -> idempotentFlowInput.send(msg2))
				.isExactlyInstanceOf(MessageRejectedException.class)
				.hasMessageContaining("rejected duplicate Message");
	}

}
