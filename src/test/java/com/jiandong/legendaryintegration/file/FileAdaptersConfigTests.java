package com.jiandong.legendaryintegration.file;

import java.io.File;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;

@EnableIntegration
@SpringBootTest(classes = {FileAdaptersConfig.class}) // using @SpringBootTest for autoloading required yml properties
@DirtiesContext
class FileAdaptersConfigTests {

	@Autowired
	@Qualifier("fileWritingFlow.input")
	MessageChannel fileWritingChannel;

	@Autowired
	@Qualifier("fileSourcePollingAdapter")
	SourcePollingChannelAdapter fileSourcePollingAdapter;

	@Autowired
	QueueChannel fileOutputChannel;

	@Test
	void happyFlow() {

		fileWritingChannel.send(MessageBuilder.withPayload("Hello World!").build());

		fileSourcePollingAdapter.start();

		Message<?> message = fileOutputChannel.receive(10000);
		Assertions.assertThat(message)
				.extracting(Message::getPayload)
				.isInstanceOfSatisfying(File.class, file -> {
					Assertions.assertThat(file.getName()).isEqualTo("foo.txt");
					Assertions.assertThat(file).content().isEqualTo("Hello World!");
				});
		fileSourcePollingAdapter.stop();
	}

}
