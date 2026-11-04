package com.jiandong.legendaryintegration.mqtt;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.client.core.Mqtt5ClientManager;
import org.springframework.integration.mqtt.client.inbound.Mqtt5MessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.client.outbound.Mqtt5MessageHandler;

@Configuration
@EnableConfigurationProperties(MqttAdaptersConfig.MqttProperties.class)
class MqttAdaptersConfig {

	protected static final String TOPIC = "cat-device";

	@Bean
	Mqtt5ClientManager mqttClientManager(MqttProperties mqttProperties) {
		return new Mqtt5ClientManager(Mqtt5Client.builder()
				.serverHost(mqttProperties.host)
				.serverPort(mqttProperties.port)
				.build().getConfig());
	}

	@Bean
	IntegrationFlow mqttOutboundFlow(Mqtt5ClientManager mqttClientManager) {
		Mqtt5MessageHandler handler = new Mqtt5MessageHandler(mqttClientManager);
		return flow -> flow
				.handle(handler);
	}

	@Bean
	IntegrationFlow mqttInboundFlow(Mqtt5ClientManager mqttClientManager) {
		var adapter = new Mqtt5MessageDrivenChannelAdapter(mqttClientManager, TOPIC);
		adapter.setPayloadType(String.class);
		return IntegrationFlow.from(adapter)
				.channel("mqttOutputChannel")
				.get();
	}

	@Bean
	QueueChannel mqttOutputChannel() {
		return new QueueChannel();
	}

	@ConfigurationProperties("mqtt")
	record MqttProperties(String host, Integer port) {

	}

}
