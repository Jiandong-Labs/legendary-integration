package com.jiandong.legendaryintegration.file;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;

@Profile("file")
@Configuration
public class InboundFileConfig {

	private static final Logger logger = LoggerFactory.getLogger(InboundFileConfig.class);

	private static final String INBOUND_PATH = "/Users/majiandong/Downloads";

	@Bean
	public IntegrationFlow inboundFileFlow() {
		return IntegrationFlow
				.from(Files.inboundAdapter(new File(INBOUND_PATH))
								.patternFilter("*.txt")
								.ignoreHidden(true)
								.preventDuplicates(true)
						, e -> e.poller(Pollers.fixedDelay(10 * 1000)))
				.channel("processFileChannel")
				.get();
	}

	@Bean
	public IntegrationFlow inboundFileProcessFlow() {
		return IntegrationFlow.from("processFileChannel")
				.handle((GenericHandler<File>) (file, headers) -> {
					logger.info("Received filename {}, filePath {}", file.getName(), file.getAbsolutePath());
					logger.info("headers {}", headers);
					return null;
				})
				.get();
	}

}
