package com.jiandong.legendaryintegration.controlbus;

import com.jiandong.legendaryintegration.config.ControlBusConfig;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@EnableIntegration
@SpringBootTest(classes = {ControlBusConfig.class, BusEndpoint.class})
@ImportAutoConfiguration({WebMvcAutoConfiguration.class})
@AutoConfigureMockMvc
@DirtiesContext
class ControlBusControllerTests {

	@Autowired
	MockMvcTester mockMvcTester;

	@Test
	void callInternal() {
		mockMvcTester
				.post()
				.uri("/control-bus/busEndpoint.callInternalFlow")
				.header("Content-Type", "application/json")
				.assertThat()
				.hasStatus(HttpStatus.OK);
	}

	@Test
	void callInternalWithReturn() {
		mockMvcTester
				.post()
				.uri("/control-bus/busEndpoint.callInternalFlow")
				.header("Content-Type", "application/json")
				.content("[{\"value\": \"abc\", \"parameterType\": \"java.lang.String\"}]")
				.assertThat()
				.hasStatus(HttpStatus.OK)
				.bodyText()
				.isEqualTo("abc");
	}

}


