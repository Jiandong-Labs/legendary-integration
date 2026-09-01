package com.jiandong.legendaryintegration.zip;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.FileCopyUtils;

@SpringJUnitConfig(classes = ZipConfig.class)
@DirtiesContext
@EnableIntegration
class ZipConfigTests {

	@Autowired
	@Qualifier("zipFlow.input")
	MessageChannel zipInputChannel;

	@Autowired
	QueueChannel zipOutputChannel;

	@Autowired
	@Qualifier("unzipFlow.input")
	MessageChannel unzipInputChannel;

	@Autowired
	QueueChannel unzipOutputChannel;

	@TempDir
	File fileDir;

	@Test
	void happyFlow() throws IOException {
		// ==== zip ====
		File sourceFile = new File(fileDir, "source-file.txt");
		FileCopyUtils.copy("abc".getBytes(), sourceFile);
		Message<File> message = MessageBuilder.withPayload(sourceFile).build();

		zipInputChannel.send(message);

		Message<?> zipResult = zipOutputChannel.receive(10000);
		Assertions.assertThat(zipResult)
				.isNotNull()
				.extracting(Message::getPayload)
				.asInstanceOf(InstanceOfAssertFactories.FILE)
				.hasFileName("source-file.txt.zip")
				.matches(file -> file.getParentFile().getName().equals("ziptransformer")); // default working directory

		// ==== unzip ====
		unzipInputChannel.send(zipResult);

		Message<?> unzipResult = unzipOutputChannel.receive(10000);
		Assertions.assertThat(unzipResult)
				.isNotNull()
				.extracting(Message::getPayload)
				.asInstanceOf(InstanceOfAssertFactories.map(String.class, File.class))
				.hasSize(1)
				.extractingByKey("source-file.txt", InstanceOfAssertFactories.FILE)
				.content(Charset.defaultCharset())
				.isEqualTo("abc");
	}

}
