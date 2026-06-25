package com.jiandong.legendaryintegration.file;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.dsl.Files;

@Configuration
public class FileAdaptersConfig {

	private static final Logger log = LoggerFactory.getLogger(FileAdaptersConfig.class);

	@Value("${file.writing-dir}")
	private String writingDir;

	@Value("${file.reading-dir}")
	private String readingDir;

	@Bean
	public IntegrationFlow fileWritingFlow() {
		return flow -> flow
				.enrichHeaders((headers) -> {
					headers.header(FileHeaders.FILENAME, "foo.txt");
				})
				.handle(Files.outboundGateway(new File(writingDir)))
				.handle((p, h) -> {
					log.info("file writing results: {}", p);
					return null;
				});
	}

	@Bean
	public IntegrationFlow fileReadingFlow() {
		return IntegrationFlow
				.from(Files.inboundAdapter(new File(readingDir))
						.patternFilter("*.txt")
						.ignoreHidden(true)
						.preventDuplicates(true), e -> e
						.id("fileSourcePollingAdapter")
						.poller(Pollers.fixedDelay(10 * 1000))
						.autoStartup(false)
				)
				.<File>handle((file, headers) -> {
					log.info("Received filename {}, filePath {}", file.getName(), file.getAbsolutePath());
					log.info("headers {}", headers);
					return file;
				})
				.channel("fileOutputChannel")
				.get();
	}

	@Bean
	public QueueChannel fileOutputChannel() {
		return new QueueChannel();
	}

}
