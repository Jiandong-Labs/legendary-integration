package com.jiandong.legendaryintegration.file;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.dsl.Files;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

@Configuration
public class OutboundFileConfig implements ApplicationRunner {

	@Override
	public void run(ApplicationArguments args) {
		generateFileChannel().send(MessageBuilder.withPayload("Hello World!").build());
	}

	private static final Logger logger = LoggerFactory.getLogger(OutboundFileConfig.class);

	private static final String OUTBOUND_PATH = "/Users/majiandong/Downloads";

	@Bean
	public MessageChannel generateFileChannel() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow outboundFileFlow() {
		return IntegrationFlow.from(generateFileChannel())
				.enrichHeaders((headers) -> {
					headers.header(FileHeaders.FILENAME, "foo.txt");
				})
				.handle(Files.outboundGateway(new File(OUTBOUND_PATH)))
				.handle((p, h) -> {
					logger.info("file writing results: {}", p);
					return null;
				})
				.get();
	}

}
