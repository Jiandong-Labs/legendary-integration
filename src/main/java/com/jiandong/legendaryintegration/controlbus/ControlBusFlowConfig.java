package com.jiandong.legendaryintegration.controlbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.Message;

@Configuration(proxyBeanMethods = false)
class ControlBusFlowConfig {

	private static final Logger log = LoggerFactory.getLogger(ControlBusFlowConfig.class);

	@Bean
	IntegrationFlow controlBus() {
		return flow -> flow
				.controlBus()
				.handle(Message.class, (payload, headers) -> {
					log.info("bus response: {}", payload);
					return payload;
				})
				.channel("controlBusOutput");
	}

	@Bean
	QueueChannel controlBusOutput() {
		return new QueueChannel();
	}

}
