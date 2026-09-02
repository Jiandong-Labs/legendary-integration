package com.jiandong.legendaryintegration.advice;

import com.jiandong.legendaryintegration.IntegrationTest;
import com.jiandong.legendaryintegration.config.IntegrationErrorConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;

@IntegrationTest({RetryableFlowConfig.class, IntegrationErrorConfig.class})
class RetryableFlowConfigTests {

	@Autowired
	@Qualifier("retryableFlow.input")
	MessageChannel retryableFlowInputChannel;

	@Autowired
	@Qualifier("retryErrorOutputChannel")
	QueueChannel retryErrorOutputChannel;

	@Test
	void retryableFlowTest() {
		Message<String> msg = MessageBuilder.withPayload("testRetry").build();
		retryableFlowInputChannel.send(msg);

		Message<?> message = retryErrorOutputChannel.receive(10000);
		Assertions.assertThat(message).isInstanceOfSatisfying(ErrorMessage.class, errorMessage ->
				Assertions.assertThat((MessagingException) errorMessage.getPayload())
						.extracting(MessagingException::getFailedMessage)
						.extracting(Message::getPayload)
						.isEqualTo("testRetry"));

	}

}
