package com.jiandong.legendaryintegration.dynamic;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@EnableIntegration
@SpringJUnitConfig(DynamicRuntimeFlowConfig.class)
@DirtiesContext
class DynamicRuntimeFlowConfigTests {

	@Autowired
	IntegrationFlowContext integrationFlowContext;

	@Autowired
	QueueChannel sharedOutputChannel;

	@Test
	void happyFlow() {
		var registry = integrationFlowContext.getRegistry();

		Assertions.assertThat(registry).containsOnlyKeys("dynamicFlow1", "dynamicFlow2");

		var dynamicFlow1 = registry.get("dynamicFlow1");
		dynamicFlow1.start();
		Message<?> flow1Msg = sharedOutputChannel.receive(10000);
		Assertions.assertThat(flow1Msg)
				.extracting(Message::getPayload)
				.isEqualTo("source1:1");
		dynamicFlow1.stop();

		var dynamicFlow2 = registry.get("dynamicFlow2");
		dynamicFlow2.start();
		Message<?> flow2Msg = sharedOutputChannel.receive(10000);
		Assertions.assertThat(flow2Msg)
				.extracting(Message::getPayload)
				.isEqualTo("source2:1");
		dynamicFlow2.stop();
	}

}
