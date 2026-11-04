package com.jiandong.legendaryintegration.mqtt;

import com.jiandong.legendaryintegration.IntegrationTest;
import com.jiandong.legendaryintegration.testcontainer.MqttContainerTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.mqtt.client.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest({MqttAdaptersConfig.class, MqttAdaptersConfigTests.Config.class})
class MqttAdaptersConfigTests implements MqttContainerTest {

	@Autowired
	@Qualifier("mqttOutboundFlow.input")
	MessageChannel mqttOutboundFlowInput;

	@Autowired
	QueueChannel mqttOutputChannel;

	@Test
	void happyFlow() {
		Message<String> message = MessageBuilder.withPayload("Hello MQTT")
				.setHeader(MqttHeaders.TOPIC, MqttAdaptersConfig.TOPIC)
				.build();

		mqttOutboundFlowInput.send(message);

		Message<?> receivedMessage = mqttOutputChannel.receive(10000);
		assertThat(receivedMessage)
				.extracting(Message::getPayload)
				.isEqualTo("Hello MQTT");
	}

	@TestConfiguration
	public static class Config {

		@Bean
		MqttAdaptersConfig.MqttProperties mqttProperties() {
			return new MqttAdaptersConfig.MqttProperties(MQTT_CONTAINER.getHost(), MQTT_CONTAINER.getMappedPort(1883));
		}

	}

}
