package com.jiandong.legendaryintegration.sftp;

import java.io.File;

import org.apache.sshd.sftp.client.SftpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.QueueChannel;
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
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SftpInboundFileConfig.SftpProperties.class)
public class SftpInboundFileConfig {

	private static final Logger log = LoggerFactory.getLogger(SftpInboundFileConfig.class);

	@ConfigurationProperties("sftp")
	public record SftpProperties(String host, Integer port, String user, String password,
								 String metaDataDir, String remoteDir, String localDir) {

	}

	@Bean
	public SessionFactory<SftpClient.DirEntry> sftpSessionFactory(SftpProperties sftpProperties) {
		DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
		factory.setHost(sftpProperties.host());
		factory.setPort(sftpProperties.port());
		factory.setUser(sftpProperties.user());
		factory.setPassword(sftpProperties.password());
		factory.setAllowUnknownKeys(true);
		return new CachingSessionFactory<>(factory);
	}

	@Bean
	public ConcurrentMetadataStore remoteFileMetaStore(SftpProperties sftpProperties) {
		var metadataStore = new PropertiesPersistingMetadataStore();
		metadataStore.setBaseDirectory(sftpProperties.metaDataDir);
		metadataStore.setFileName("sftp_meta_data.properties");
		metadataStore.afterPropertiesSet();
		return metadataStore;
	}

	@Bean
	public CompositeFileListFilter<SftpClient.DirEntry> remoteSftpFileFilter(SftpProperties sftpProperties) {
		var remoteFileNameStore = new PropertiesPersistingMetadataStore();
		remoteFileNameStore.setBaseDirectory(sftpProperties.metaDataDir);
		remoteFileNameStore.setFileName("sftp_file.properties");
		remoteFileNameStore.afterPropertiesSet();

		var compositeFileListFilter = new CompositeFileListFilter<SftpClient.DirEntry>();
		var sftpFileNameStore = new SftpPersistentAcceptOnceFileListFilter(remoteFileNameStore, "sftpFileStore");
		sftpFileNameStore.setFlushOnUpdate(true);
		compositeFileListFilter.addFilters(
				new SftpRegexPatternFileListFilter("^.*\\.txt$"),
				sftpFileNameStore);
		return compositeFileListFilter;
	}

	@Bean
	public CompositeFileListFilter<File> localFileFilter(SftpProperties sftpProperties) {
		var localFileNameStore = new PropertiesPersistingMetadataStore();
		localFileNameStore.setBaseDirectory(sftpProperties.metaDataDir);
		localFileNameStore.setFileName("local_file.properties");
		localFileNameStore.afterPropertiesSet();

		var fileListFilter = new CompositeFileListFilter<File>();
		var localFileStore = new FileSystemPersistentAcceptOnceFileListFilter(localFileNameStore, "localFileStore");
		localFileStore.setFlushOnUpdate(true);
		fileListFilter
				.addFilter(new IgnoreHiddenFileListFilter())
				.addFilter(localFileStore);
		return fileListFilter;
	}

	@Bean
	public IntegrationFlow sftpInboundFileFlow(SessionFactory<SftpClient.DirEntry> sftpSessionFactory,
			SftpProperties sftpProperties, CompositeFileListFilter<SftpClient.DirEntry> remoteSftpFileFilter,
			CompositeFileListFilter<File> localFileFilter, ConcurrentMetadataStore remoteFileMetaStore) {
		return IntegrationFlow
				.from(Sftp.inboundAdapter(sftpSessionFactory)
						.preserveTimestamp(true)
						.remoteDirectory(sftpProperties.remoteDir)
						.filter(remoteSftpFileFilter) // remote filter: Prevents re-downloading from SFTP
						.localDirectory(new File(sftpProperties.localDir))
						.localFilter(localFileFilter)
						.remoteFileMetadataStore(remoteFileMetaStore) // remote file metadata retrieval, not for filtering.
						.maxFetchSize(10)
						.autoCreateLocalDirectory(true), e -> e
						.id("sftpInboundAdapter")
						.autoStartup(false)
						.poller(Pollers.fixedDelay(10 * 1000)))
				.handle((GenericHandler<File>) (file, headers) -> {
					log.info("Received filename {}, filePath {}", file.getName(), file.getAbsolutePath());
					log.info("headers {}", headers);
					return file;
				})
				.channel("sftpFileOutputChannel")
				.get();
	}

	@Bean
	QueueChannel sftpFileOutputChannel() {
		return new QueueChannel();
	}

}
