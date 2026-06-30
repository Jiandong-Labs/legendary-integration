package com.jiandong.legendaryintegration.sftp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class SftpTestSupport {

	static final SshServer SFTP_SERVER = SshServer.setUpDefaultServer();

	static final String SFTP_HOST = "localhost";

	static final String SFTP_USER = "sftp";

	static final String SFTP_PWD = "pwd";

	@TempDir
	static Path SFTP_REMOTE_ROOT;

	@BeforeAll
	static void startup() throws IOException {
		SFTP_SERVER.setPort(0);
		SFTP_SERVER.setPasswordAuthenticator((username, password, session) ->
				SFTP_USER.equals(username) && SFTP_PWD.equals(password));
		SFTP_SERVER.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
		SFTP_SERVER.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
		SFTP_SERVER.setFileSystemFactory(new VirtualFileSystemFactory(SFTP_REMOTE_ROOT));
		SFTP_SERVER.start();
	}

	@DynamicPropertySource
	static void sftpConfig(DynamicPropertyRegistry registry) {
		registry.add("sftp.host", () -> SFTP_HOST);
		registry.add("sftp.port", SFTP_SERVER::getPort);
		registry.add("sftp.user", () -> SFTP_USER);
		registry.add("sftp.password", () -> SFTP_PWD);
	}

	@AfterAll
	static void shutdown() throws IOException {
		SFTP_SERVER.stop();
	}

}
