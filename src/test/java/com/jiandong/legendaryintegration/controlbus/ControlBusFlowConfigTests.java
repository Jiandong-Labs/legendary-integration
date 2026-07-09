package com.jiandong.legendaryintegration.controlbus;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@EnableIntegration
@SpringJUnitConfig({ControlBusFlowConfig.class, BusEndpoint.class})
@DirtiesContext
class ControlBusFlowConfigTests {

	@Autowired
	@Qualifier("controlBus.input")
	MessageChannel controlBusInputChannel;

	@MockitoSpyBean
	BusEndpoint busEndpoint;

	@Autowired
	@Qualifier("controlBusOutput")
	QueueChannel controlBusOutput;

	@Test
	void happyFlow() {
		Message<String> command1 = MessageBuilder.withPayload("busEndpoint.callInternalFlow").build();
		controlBusInputChannel.send(command1);
		Mockito.verify(busEndpoint, Mockito.times(1)).callInternalFlow();

		Message<String> command2 = MessageBuilder.withPayload("busEndpoint.callInternalFlow")
				.setHeader(IntegrationMessageHeaderAccessor.CONTROL_BUS_ARGUMENTS, List.of("message from control bus"))
				.build();
		controlBusInputChannel.send(command2);
		Mockito.verify(busEndpoint, Mockito.times(1)).callInternalFlow(Mockito.anyString());
		Message<?> message = controlBusOutput.receive(10000);
		Assertions.assertThat(message).extracting(Message::getPayload).isEqualTo("message from control bus");
	}

}
