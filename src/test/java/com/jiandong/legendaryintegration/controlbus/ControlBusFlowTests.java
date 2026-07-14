package com.jiandong.legendaryintegration.controlbus;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@EnableIntegration
@SpringBootTest(classes = {ControlBusFlowConfig.class, CustomBusEndpoint.class})
@ImportAutoConfiguration({WebMvcAutoConfiguration.class})
@AutoConfigureMockMvc
@DirtiesContext
class ControlBusFlowTests {

	@Autowired
	@Qualifier("pureControlBusFlow.input")
	MessageChannel pureControlBusInputChannel;

	@MockitoSpyBean
	CustomBusEndpoint customBusEndpoint;

	@Autowired
	@Qualifier("controlBusOutput")
	QueueChannel controlBusOutput;

	@Test
	void pureControlBusFlow() {
		Message<String> command1 = MessageBuilder.withPayload("customBusEndpoint.callInternalFlow").build();
		pureControlBusInputChannel.send(command1);
		Mockito.verify(customBusEndpoint, Mockito.times(1)).callInternalFlow();

		Message<String> command2 = MessageBuilder.withPayload("customBusEndpoint.callInternalFlow")
				.setHeader(IntegrationMessageHeaderAccessor.CONTROL_BUS_ARGUMENTS, List.of("message from control bus"))
				.build();
		pureControlBusInputChannel.send(command2);
		Mockito.verify(customBusEndpoint, Mockito.times(1)).callInternalFlow(Mockito.anyString());
		Message<?> message = controlBusOutput.receive(10000);
		Assertions.assertThat(message).extracting(Message::getPayload).isEqualTo("message from control bus");
	}

	// ============= control bus controller test ==================

	@Autowired
	MockMvcTester mockMvcTester;

	@Test
	void callInternal() {
		mockMvcTester
				.post()
				.uri("/control-bus/customBusEndpoint.callInternalFlow")
				.header("Content-Type", "application/json")
				.assertThat()
				.hasStatus(HttpStatus.OK);
	}

	@Test
	void callInternalWithReturn() {
		mockMvcTester
				.post()
				.uri("/control-bus/customBusEndpoint.callInternalFlow")
				.header("Content-Type", "application/json")
				.content("[{\"value\": \"abc\", \"parameterType\": \"java.lang.String\"}]")
				.assertThat()
				.hasStatus(HttpStatus.OK)
				.bodyText()
				.isEqualTo("abc");
	}

}
