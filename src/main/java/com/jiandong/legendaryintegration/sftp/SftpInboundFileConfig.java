package com.jiandong.legendaryintegration.sftp;

import java.io.File;

import org.apache.sshd.sftp.client.SftpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

@Profile("sftp")
@Configuration
public class SftpInboundFileConfig {

	private static final Logger log = LoggerFactory.getLogger(SftpInboundFileConfig.class);

	private static final String LOCAL_PATH = "/Users/majiandong/Downloads/SFTP_Local";

	private static final String REMOTE_PATH = "/Users/majiandong/Downloads/SFTP_Remote";

	@Bean
	public SessionFactory<SftpClient.DirEntry> sftpSessionFactory() {
		DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
		factory.setHost("localhost");
		factory.setPort(22);
		factory.setUser("majiandong");
		factory.setPassword("2026");
		factory.setAllowUnknownKeys(true);
		return new CachingSessionFactory<>(factory);
	}

	@Bean
	public IntegrationFlow sftpInboundFileFlow() {
		return IntegrationFlow
				.from(Sftp.inboundAdapter(sftpSessionFactory())
						.preserveTimestamp(true)
						.remoteDirectory(REMOTE_PATH)
						.regexFilter(".*\\.txt$")
						.localDirectory(new File(LOCAL_PATH))
						.maxFetchSize(10)
						.autoCreateLocalDirectory(true), e -> e
						.id("sftpInboundAdapter")
						.autoStartup(true)
						.poller(Pollers.fixedDelay(10 * 1000)))
				.handle((GenericHandler<File>) (file, headers) -> {
					log.info("Received filename {}, filePath {}", file.getName(), file.getAbsolutePath());
					log.info("headers {}", headers);
					return null;
				})
				.get();
	}

}
