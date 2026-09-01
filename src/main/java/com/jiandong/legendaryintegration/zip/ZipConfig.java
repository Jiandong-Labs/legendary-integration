package com.jiandong.legendaryintegration.zip;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.zip.transformer.UnZipTransformer;
import org.springframework.integration.zip.transformer.ZipTransformer;

@Configuration(proxyBeanMethods = false)
class ZipConfig {

	@Bean
	IntegrationFlow zipFlow() {
		return flow -> flow
				.transform(new ZipTransformer())
				.channel("zipOutputChannel");
	}

	@Bean
	QueueChannel zipOutputChannel() {
		return new QueueChannel();
	}

	@Bean
	IntegrationFlow unzipFlow() {
		return flow -> flow
				.transform(new UnZipTransformer())
				.channel("unzipOutputChannel");
	}

	@Bean
	QueueChannel unzipOutputChannel() {
		return new QueueChannel();
	}

}
