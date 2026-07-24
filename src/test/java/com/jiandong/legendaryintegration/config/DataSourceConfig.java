package com.jiandong.legendaryintegration.config;

import javax.sql.DataSource;

import com.jiandong.legendaryintegration.testcontainer.PostgresContainerTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.JdbcTransactionManager;

@TestConfiguration(proxyBeanMethods = false)
public class DataSourceConfig {

	@Bean
	DataSource dataSource() {
		return PostgresContainerTest.postgresqlDataSource();
	}

	@Bean
	JdbcTransactionManager transactionManager(DataSource dataSource) {
		return new JdbcTransactionManager(dataSource);
	}

	@Bean
	JdbcClient jdbcClient(DataSource dataSource) {
		return JdbcClient.create(dataSource);
	}

}
