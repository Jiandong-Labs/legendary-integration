package com.jiandong.legendaryintegration.controlbus;

import java.util.List;

import com.jiandong.legendaryintegration.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@IntegrationTest({ControlBusGatewayConfig.class, CustomBusEndpoint.class})
class ControlBusGatewayConfigTests {

	@Autowired
	@Qualifier("controlBusGateway")
	private ControlBusGatewayConfig.ControlBusGateway controlBusGateway;

	@Test
	void gatewayHappyFlow() {
		Object noResponse = controlBusGateway.send("customBusEndpoint.callInternalFlow");
		Assertions.assertThat(noResponse).isNull();

		Object response = controlBusGateway.send("customBusEndpoint.callInternalFlow", List.of("abc"));
		Assertions.assertThat(response).isEqualTo("gateway:abc");
	}

}
