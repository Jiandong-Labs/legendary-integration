package com.jiandong.legendaryintegration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.http.config.EnableIntegrationGraphController;

@EnableIntegrationGraphController
@SpringBootApplication
public class LegendaryIntegrationApplication {

	static void main(String[] args) {
		SpringApplication.run(LegendaryIntegrationApplication.class, args);
	}

}
