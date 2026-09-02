package com.jiandong.legendaryintegration.gateway;

import com.jiandong.legendaryintegration.IntegrationBootTest;
import com.jiandong.legendaryintegration.controlbus.ControlBusGatewayConfig;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@IntegrationBootTest(classes = {GatewayController.class, ControlBusGatewayConfig.class})
@ImportAutoConfiguration({WebMvcAutoConfiguration.class})
@AutoConfigureMockMvc
class GatewayControllerTests {

	@Autowired
	MockMvcTester mockMvcTester;

	@Test
	void disableLogging() {
		mockMvcTester
				.post()
				.uri("/gateway/control-bus/errorChannel.setLoggingEnabled")
				.header("Content-Type", "application/json")
				.content("[false]")
				.assertThat()
				.hasStatus(HttpStatus.OK);
	}

}
