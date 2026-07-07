package com.jiandong.legendaryintegration.advice;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;

@EnableIntegration
@SpringJUnitConfig({TransactionFlowConfig.class})
@DirtiesContext
class TransactionFlowConfigTests {

	@MockitoSpyBean
	PlatformTransactionManager transactionManager;

	@Autowired
	@Qualifier("transactionFlow.input")
	MessageChannel transactionFlowInput;

	@Test
	void transactionFlowTest() {
		Message<String> msg = MessageBuilder.withPayload("test").build();
		transactionFlowInput.send(msg);

		Mockito.verify(transactionManager, Mockito.times(1)).getTransaction(Mockito.any());
		Mockito.verify(transactionManager, Mockito.times(1)).commit(Mockito.any());
	}

}
