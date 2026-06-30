package com.jiandong.legendaryintegration.sftp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@EnableIntegration
@SpringJUnitConfig(SftpInboundFileConfig.class)
@DirtiesContext
class SftpInboundFileConfigTests extends SftpTestSupport {

	@TempDir
	static Path localDownloadDir;

	@TempDir
	static Path metaStoreDir;

	@Autowired
	SftpInboundFileConfig.SftpProperties sftpProperties;

	@Autowired
	SftpInboundFileConfig.SftpDirs sftpDirs;

	@Autowired
	@Qualifier("sftpSourcePollingAdapter")
	SourcePollingChannelAdapter sftpSourcePollingAdapter;

	@Autowired
	private QueueChannel sftpFileOutputChannel;

	@DynamicPropertySource
	static void sftpConfig(DynamicPropertyRegistry registry) {
		registry.add("sftp.dirs.remote-dir", () -> "/");
		registry.add("sftp.dirs.local-dir", localDownloadDir::toString);
		registry.add("sftp.dirs.meta-data-dir", metaStoreDir::toString);
	}

	@Test
	void happyFlow() throws IOException {
		assertThat(sftpProperties).satisfies(props -> {
			assertThat(props.host()).isEqualTo(SFTP_HOST);
			assertThat(props.port()).isEqualTo(SFTP_SERVER.getPort());
			assertThat(props.user()).isEqualTo(SFTP_USER);
			assertThat(props.password()).isEqualTo(SFTP_PWD);
		});
		assertThat(sftpDirs).satisfies(props -> {
			assertThat(props.remoteDir()).isEqualTo("/");
			assertThat(props.localDir()).isEqualTo(localDownloadDir.toString());
			assertThat(props.metaDataDir()).isEqualTo(metaStoreDir.toString());
		});

		Path remoteFile = SFTP_REMOTE_ROOT.resolve("./test.txt");
		Files.writeString(remoteFile, "this is a test file from sftp", StandardCharsets.UTF_8);

		sftpSourcePollingAdapter.start();
		Message<?> receivedMsg = sftpFileOutputChannel.receive(10000);

		Consumer<File> fileConsumer = (downloadedFile) -> {
			try {
				assertThat(downloadedFile.getName()).isEqualTo("test.txt");
				assertThat(downloadedFile.getParentFile()).isEqualTo(localDownloadDir.toFile());
				assertThat(Files.readString(downloadedFile.toPath())).isEqualTo("this is a test file from sftp");
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
		assertThat(receivedMsg).extracting(Message::getPayload).isInstanceOfSatisfying(File.class, fileConsumer);
		assertThat(metaStoreDir.resolve("sftp_meta_data.properties")).exists();
		assertThat(metaStoreDir.resolve("sftp_file.properties")).exists();
		assertThat(metaStoreDir.resolve("local_file.properties")).exists();

		sftpSourcePollingAdapter.stop();
	}

}
