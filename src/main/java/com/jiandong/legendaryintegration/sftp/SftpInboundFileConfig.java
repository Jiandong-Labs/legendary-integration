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
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.FileSystemPersistentAcceptOnceFileListFilter;
import org.springframework.integration.file.filters.IgnoreHiddenFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.metadata.PropertiesPersistingMetadataStore;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.integration.sftp.filters.SftpPersistentAcceptOnceFileListFilter;
import org.springframework.integration.sftp.filters.SftpRegexPatternFileListFilter;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

@Profile("sftp")
@Configuration
public class SftpInboundFileConfig {

	private static final Logger log = LoggerFactory.getLogger(SftpInboundFileConfig.class);

	private static final String LOCAL_PATH = "/Users/majiandong/Downloads/SFTP_Local";

	private static final String REMOTE_PATH = "/Users/majiandong/Downloads/SFTP_Remote";

	private static final String META_DATA = "/Users/majiandong/Downloads/Meta_Data";

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
	public ConcurrentMetadataStore remoteFileNameStore() {
		var metadataStore = new PropertiesPersistingMetadataStore();
		metadataStore.setBaseDirectory(META_DATA);
		metadataStore.setFileName("sftp_file.properties");
		metadataStore.afterPropertiesSet();
		return metadataStore;
	}

	@Bean
	public ConcurrentMetadataStore remoteFileMetaStore() {
		var metadataStore = new PropertiesPersistingMetadataStore();
		metadataStore.setBaseDirectory(META_DATA);
		metadataStore.setFileName("sftp_meta_data.properties");
		metadataStore.afterPropertiesSet();
		return metadataStore;
	}

	@Bean
	public ConcurrentMetadataStore localFileNameStore() {
		var metadataStore = new PropertiesPersistingMetadataStore();
		metadataStore.setBaseDirectory(META_DATA);
		metadataStore.setFileName("local_file.properties");
		metadataStore.afterPropertiesSet();
		return metadataStore;
	}

	private CompositeFileListFilter<SftpClient.DirEntry> remoteSftpFileFilter() {
		var compositeFileListFilter = new CompositeFileListFilter<SftpClient.DirEntry>();
		compositeFileListFilter.addFilters(
				new SftpRegexPatternFileListFilter("^.*\\.txt$"),
				new SftpPersistentAcceptOnceFileListFilter(remoteFileNameStore(), "sftpFileStore"));
		return compositeFileListFilter;
	}

	private CompositeFileListFilter<File> localFileFilter() {
		var fileListFilter = new CompositeFileListFilter<File>();
		fileListFilter
				.addFilter(new IgnoreHiddenFileListFilter())
				.addFilter(new FileSystemPersistentAcceptOnceFileListFilter(localFileNameStore(), "localFileStore"));
		return fileListFilter;
	}

	@Bean
	public IntegrationFlow sftpInboundFileFlow() {
		return IntegrationFlow
				.from(Sftp.inboundAdapter(sftpSessionFactory())
						.preserveTimestamp(true)
						.remoteDirectory(REMOTE_PATH)
						.filter(remoteSftpFileFilter()) // remote filter: Prevents re-downloading from SFTP
						.localDirectory(new File(LOCAL_PATH))
						.localFilter(localFileFilter())
						.remoteFileMetadataStore(remoteFileMetaStore()) // remote file metadata retrieval, not for filtering.
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
